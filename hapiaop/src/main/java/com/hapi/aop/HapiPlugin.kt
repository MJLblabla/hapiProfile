package com.hapi.aop

import com.hapi.aop.trans.BlockTracer
import com.hapi.aop.trans.FrameTracer

object HapiPlugin {


    init {
        LooperMonitor.registerLoopListener(BlockTracer(), true);
        ChoreographerMonitor.fpsListerListers.add(FrameTracer())
        MethodBeatMonitor.init()
    }

    fun startAllMonitor(){
        startLooperMonitor()
        startChoreographerMonitor()
    }

    fun startLooperMonitor() {
        LooperMonitor.start()
    }

    fun startChoreographerMonitor() {
        ChoreographerMonitor.start()
    }

    fun stopLooperMonitor() {
        LooperMonitor.stop()
    }

    fun stopChoreographerMonitor() {
        ChoreographerMonitor.start()
    }

}