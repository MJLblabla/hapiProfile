package com.hapi.aop.trans

import android.util.Log
import com.hapi.aop.LooperMonitor
import com.hapi.aop.MethodBeatMonitor

class BlockTracer(val blockCostIssue:Int = 2000)  : LooperMonitor.LoopListener {

    private val TAG = "ChoreographerMonitor"
    private var startTime = 0

    override fun dispatchMsgStart() {
        startTime = MethodBeatMonitor.time
    }

    override fun dispatchMsgStop() {
        val endTime =  MethodBeatMonitor.time
        val timeCost = startTime - endTime
        if(timeCost > blockCostIssue){
            Log.d(TAG, "主线程卡顿 $timeCost")
            MethodBeatMonitor.issue()
        }
    }
}