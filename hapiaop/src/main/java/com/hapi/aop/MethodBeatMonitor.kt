package com.hapi.aop

import android.os.Looper
import android.util.Log
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.*

object MethodBeatMonitor  {

    private final val Tag = "MethodBeatMonitor"
    /**
     * 过滤短耗时
     */
    private var minCostFilter = -1

    var time = 0
    private set

    private var jobTime: Job? = null

    private val sMainThreadId = Looper.getMainLooper().thread.id
    private var mMethodNode: MethodBeat? = null
    private var mRootNode: MethodBeat? = null


    private fun startTimer() {

        time = 0
        jobTime = GlobalScope.launch(Dispatchers.Default) {
            time += 5
            delay(5)
        }
    }

    interface BeatLister {
        fun onBeatOnce()
    }

    fun init() {
        LooperMonitor.registerLoopListener(object : LooperMonitor.LoopListener {

            override fun dispatchMsgStart() {
                startTimer()
            }

            override fun dispatchMsgStop() {
                jobTime?.cancel()
                jobTime=null
            }
        }, true, true)
    }

    fun logS(methodSign: String) {
        Log.d(Tag,"logS ${methodSign}")
        if (Thread.currentThread().id != sMainThreadId) {
            return
        }
        if(jobTime==null){
            return
        }
        val beat = MethodBeat(methodSign, -1, mMethodNode)
        if(mRootNode==null){
            mRootNode=beat
        }
        if(mMethodNode==null){
            mMethodNode=beat
        }else{
            if(mMethodNode!!.cost>=0){
                mMethodNode!!.parent?.zChild?.add(beat)
            }else{
                mMethodNode!!.zChild.add(beat)
            }
            mMethodNode=beat
        }
        mMethodNode?.startTime = time
    }

    fun logE(methodSign: String) {
        Log.d(Tag,"logE ${methodSign}")
        if (Thread.currentThread().id != sMainThreadId) {
            return
        }
        if(mMethodNode==null){
            return
        }
        val cost = time - mMethodNode!!.startTime
        mMethodNode?.cost=cost
        if(cost> minCostFilter){

        }else{
            val parent = mMethodNode?.parent
            mMethodNode?.parent?.zChild?.remove(mMethodNode!!)
            mMethodNode?.parent=null

            mMethodNode=parent
        }
//        if (mMethodBeatsTemp.isEmpty()) {
//          return
//        }
//
//        val lastBeat = mMethodBeatsTemp.last
//        val cost = time - lastBeat.startTime
//        val isEndForLast = lastBeat.methodSign == methodSign
//        if (isEndForLast && cost < minCostFilter) {
//            mMethodBeatsTemp.removeLast()
//        } else {
//            val temp = MethodBeatTemp(methodSign, time)
//            mMethodBeatsTemp.add(temp)
//
//            if (mMethodNode == null) {
//                mMethodNode = MethodBeat(methodSign, cost)
//                mRootNode = mMethodNode
//            } else {
//                val beat = MethodBeat(methodSign, cost, mMethodNode)
//                if (isEndForLast) {
//                    mMethodNode!!.child.add(beat)
//                } else {
//                    mMethodNode!!.parent?.child?.add(beat)
//                }
//                mMethodNode = beat
//            }
//        }
    }



    fun issue(){
        Log.d(Tag,"logS issue")
        mRootNode?.let {
            Log.d(Tag,   JSON.toJSONString(it))
            JSON.toJSON(it)
        }
    }
}