package com.hapi.aop

import android.os.Looper
import android.util.Log
import com.hapi.aop.util.DeviceUtil
import java.util.*

object MethodBeatMonitor {

    private final val Tag = "MethodBeatMonitor"

    /**
     * 过滤短耗时
     */
    private var minCostFilter = 5

    private val sMainThreadId = Looper.getMainLooper().thread.id
    private val beatStack = Stack<MethodBeat>()
    private val beatQueen = LinkedList<MethodBeat>()

    interface BeatLister {
        fun onBeatOnce()
    }

    fun dispatchMsgStart() {

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

        var node = beatStack.pop()

        if (node.methodSign != methodSign&& !beatStack.isEmpty()) {
            while (!beatStack.isEmpty()){
                node = beatStack.pop()
                if(node.methodSign == methodSign){
                    break
                }
            }
        }
        if(node.methodSign==methodSign){
            val cost = LoopTimer.time - node.startTime
            node.cost = cost

            if (cost > minCostFilter) {
                beatQueen.addFirst(node)
            }
        }
    }


    fun issue(msg: String = "") {
        beatQueen.sortWith(Comparator { o1, o2 -> o2.cost - o1.cost })

        var methodName = ""
        var cost = 0
        val iterator = beatQueen.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            val itemMethodNameArray = it.methodSign?.split(".")
            val name = itemMethodNameArray!![itemMethodNameArray.size - 1]
            if (name == methodName && it.cost == cost) {
                iterator.remove()
            }
            methodName = name
            cost = it.cost
        }

        beatQueen.forEachIndexed { index, methodBeat ->
            if (index > 30) {
                return@forEachIndexed
            }
        }

        HapiMonitorPlugin.mMonitorConfig.issureCallBack?.let {

            val issure = Issure()
            issure.msg = msg
            issure.availMemory = DeviceUtil.getAvailMemory(HapiMonitorPlugin.context)
            issure.totalMemory = DeviceUtil.getTotalMemory(HapiMonitorPlugin.context)
            issure.cpuRate = DeviceUtil.getAppCpuRate()
            issure.foregroundPageName = ActivityCollection.currentActivity?.localClassName
            issure.methodBeats = beatQueen

            it.onIssure(issure)
        }

    }


}