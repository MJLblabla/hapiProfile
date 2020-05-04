package com.hapi.aop

import android.os.Looper
import com.hapi.aop.util.DeviceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

object MethodBeatMonitor {

    private final val Tag = "MethodBeatMonitor"

    /**
     * 过滤短耗时
     */
    var minCostFilter = 5
    var methodDeep = 0
    var maxDeep = 100000;
    var issureTop = 30

    private val sMainThreadId = Looper.getMainLooper().thread.id


    fun dispatchMsgStart() {
        methodDeep = 0
        beats.clear()
    }


    private val beats = LinkedList<Beat>()

    fun checkDeep(): Boolean {

        if (Thread.currentThread().id != sMainThreadId) {
            return false
        }
        if (!LoopTimer.isStart) {
            return false
        }
        return methodDeep < maxDeep;
    }

    fun addBeat(beat: Beat) {
        methodDeep++
        beats.addFirst(beat)
    }


    fun issue(msg: String = "") {


        beats.sortWith(Comparator { o1, o2 -> o2.cost - o1.cost })

        var methodName = ""
        var cost = 0
        val iterator = beats.iterator()
        val beatTemp = LinkedList<Beat>();
        while (iterator.hasNext()) {
            val it = iterator.next()
            val itemMethodNameArray = it.sign?.split(".")
            val name = itemMethodNameArray!![itemMethodNameArray.size - 1]
            if (name == methodName && it.cost == cost) {
                iterator.remove()
            } else {
                beatTemp.add(it)
                if (beatTemp.size > issureTop) {
                    break
                }
            }
            methodName = name
            cost = it.cost
        }


        HapiMonitorPlugin.mMonitorConfig.issureCallBack?.let {

            val issure = Issure()
            issure.msg = msg
            issure.availMemory = DeviceUtil.getAvailMemory(HapiMonitorPlugin.context)
            issure.totalMemory = DeviceUtil.getTotalMemory(HapiMonitorPlugin.context)
            issure.cpuRate = DeviceUtil.getAppCpuRate()
            issure.foregroundPageName = ActivityCollection.currentActivity?.localClassName
            issure.methodBeats = beatTemp
            it.onIssure(issure)

        }


    }


}