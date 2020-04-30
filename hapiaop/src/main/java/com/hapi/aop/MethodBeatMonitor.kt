package com.hapi.aop

import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import java.util.*

object MethodBeatMonitor : IMonitor {

    /**
     * 过滤短耗时
     */
    private var minCostFilter = 10

    var time = 0
    private set

    private var jobTime: Job? = null

    private val sMainThreadId = Looper.getMainLooper().thread.id
    private var mMethodNode: MethodBeat? = null
    private val mMethodBeatsTemp = LinkedList<MethodBeatTemp>()
    private var mRootNode: MethodBeat? = null
    private var isStart = false

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

    init {
        LooperMonitor.registerLoopListener(object : LooperMonitor.LoopListener {
            override fun dispatchMsgStart() {
                startTimer()
            }

            override fun dispatchMsgStop() {
                jobTime?.cancel()
            }
        }, true, true)
    }

    fun logS(methodSign: String) {
        if (Thread.currentThread().id != sMainThreadId) {
            return
        }
        val temp = MethodBeatTemp(methodSign, time)
        mMethodBeatsTemp.add(temp)
    }

    fun logE(methodSign: String) {
        if (mMethodBeatsTemp.isEmpty()) {
            Log.d("Tag", "打点出错了")
        }
        val lastBeat = mMethodBeatsTemp.last
        val cost = time - lastBeat.startTime
        val isEndForLast = lastBeat.methodSign == methodSign
        if (isEndForLast && cost < minCostFilter) {
            mMethodBeatsTemp.removeLast()
        } else {
            val temp = MethodBeatTemp(methodSign, time)
            mMethodBeatsTemp.add(temp)
            if (mMethodNode == null) {
                mMethodNode = MethodBeat(methodSign, cost)
                mRootNode = mMethodNode
            } else {
                val beat = MethodBeat(methodSign, cost, mMethodNode)
                if (!isEndForLast) {
                    mMethodNode!!.child.add(beat)
                } else {
                    mMethodNode!!.parent?.child?.add(beat)
                }
                mMethodNode = beat
            }
        }
    }

    override fun start() {
        isStart = true
    }

    override fun stop() {
        isStart = false
    }

    override fun onForeground(isForeground: Boolean) {

    }

    fun issue(){

    }
}