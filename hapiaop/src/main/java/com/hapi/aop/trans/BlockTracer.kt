package com.hapi.aop.trans

import com.hapi.aop.HapiMonitorPlugin
import com.hapi.aop.LoopTimer
import com.hapi.aopbeat.MethodBeatMonitorJava

/**
 * 卡顿检查
 */
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
            MethodBeatMonitorJava.issue("主线程卡顿 $timeCost",timeCost)
        }
    }

}