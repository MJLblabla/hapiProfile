package com.hapi.aop

import android.os.Looper
import android.util.Log
import java.util.*

object MethodBeatMonitor {

    private final val Tag = "MethodBeatMonitor"
    /**
     * 过滤短耗时
     */
    private var minCostFilter = -1
    private val sMainThreadId = Looper.getMainLooper().thread.id
    private val beatStack = Stack<MethodBeat>()
    private val beatQueen = LinkedList<MethodBeat>()

    interface BeatLister {
        fun onBeatOnce()
    }

    fun dispatchMsgStart(){

        beatQueen.clear()
        beatStack.clear()
    }

    fun logS(methodSign: String) {

        if (Thread.currentThread().id != sMainThreadId) {
            return
        }
        if (!LoopTimer.isStart) {
            return
        }

        val beat = MethodBeat(methodSign)
        beat.startTime = LoopTimer.time
        beatStack.push(beat)
    }

    fun logE(methodSign: String) {

        if (Thread.currentThread().id != sMainThreadId) {
            return
        }
        if (beatStack.isEmpty()) {
            return
        }
        val node = beatStack.pop()

        if(node.methodSign==methodSign){
            val cost = LoopTimer.time - node.startTime
            node.cost = cost

            if (cost > minCostFilter) {
                beatQueen.addFirst(node)
            }
        }else{
            Log.d(Tag," iterator" + methodSign)
        }

    }


    fun issue(): LinkedList<MethodBeat> {

        beatQueen.sortWith(Comparator { o1, o2 -> o2.cost - o1.cost })

        var methodName = ""
        var cost =0
        val iterator = beatQueen.iterator()
        while (iterator.hasNext()){
            val it = iterator.next()
            val itemMethodNameArray = it.methodSign?.split(".")
            val name = itemMethodNameArray!![itemMethodNameArray.size-1]
            if(name==methodName&&it.cost==cost){
                iterator.remove()
                if(methodName=="onCreate"){
                    Log.d(Tag," iterator.remove()" + it.methodSign)
                }
            }
            methodName=name
            cost=it.cost
        }

        beatQueen.forEachIndexed { index, methodBeat ->
            if (index > 30) {
                return@forEachIndexed
            }
            Log.d(Tag, "top  ${index}  ${methodBeat.methodSign}  cost ${methodBeat.cost}")
        }
        return beatQueen
    }
}