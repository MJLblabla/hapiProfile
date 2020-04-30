package com.hapi.aop.trans

import android.util.Log
import android.view.Choreographer
import com.hapi.aop.ChoreographerMonitor
import com.hapi.aop.MethodBeatMonitor

class FrameTracer(private var frameCostIssues:Int = 20) :ChoreographerMonitor.FpsLister {

    private val TAG = "ChoreographerMonitor"

    override fun onFrame(currentFrameCost: Long) {
        if(currentFrameCost>frameCostIssues){
            Log.d(TAG,"单帧耗时过大 ${currentFrameCost}")
            MethodBeatMonitor.issue()
        }
    }

    override fun onLastSecCost(frameRate: Int) {
        Log.d(TAG,"帧率 ${frameRate}")
    }
}