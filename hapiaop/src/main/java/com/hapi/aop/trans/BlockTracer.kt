package com.hapi.aop.trans

import android.util.Log
import com.hapi.aop.HapiMonitorPlugin
import com.hapi.aop.LoopTimer
import com.hapi.aop.MethodBeatMonitor

class BlockTracer : ITracer() {

    private val TAG = "BlockTracer"
    private var startTime = 0


    override fun loopStart() {
        super.loopStart()
        startTime = LoopTimer.time

    }

    override fun loopStop() {
        super.loopStop()
        val endTime = LoopTimer.time
        val timeCost = endTime - startTime

        if (timeCost > HapiMonitorPlugin.mMonitorConfig.blockCostIssue) {
            MethodBeatMonitor.issue("主线程卡顿 $timeCost")
        }
    }

}