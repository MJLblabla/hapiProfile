package com.hapi.aop.trans

import android.util.Log
import com.hapi.aop.LoopTimer
import com.hapi.aop.LooperMonitor
import com.hapi.aop.MethodBeatMonitor

object BlockTracer : ITracer() {

    private val TAG = "ChoreographerMonitor"
    private var startTime = 0
    val blockCostIssue: Int = 20

    override fun loopStart() {
        super.loopStart()
        startTime = LoopTimer.time
    }

    override fun loopStop() {
        super.loopStop()
        val endTime = LoopTimer.time
        val timeCost = endTime - startTime
        if (timeCost > blockCostIssue) {
            Log.d(TAG, "主线程卡顿 $timeCost")
            MethodBeatMonitor.issue()
        }
    }

}