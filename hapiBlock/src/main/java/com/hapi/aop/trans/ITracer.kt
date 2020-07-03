package com.hapi.aop.trans

import com.hapi.aop.LoopTimer
import com.hapi.aop.LooperMonitor

open class ITracer(var timerNeed: Boolean = true,var loopNeed: Boolean = true) {

    protected var isStart = false
    private var loopListener = object : LooperMonitor.LoopListener {
        override fun dispatchMsgStart() {
            loopStart()
        }

        override fun dispatchMsgStop() {
            loopStop()
        }

    }

    protected open fun loopStart() {

    }

    protected open fun loopStop() {

    }

    open fun start() {
        isStart = true
        if (timerNeed) {
            LoopTimer.addTimerObsever()
        }
        if(loopNeed){
            LooperMonitor.registerLoopListener(loopListener, true)
        }
    }

    open fun stop() {
        if (timerNeed) {
            LoopTimer.removeTimerObsever()
        }
        isStart = false
        if(loopNeed){
            LooperMonitor.registerLoopListener(loopListener, false)
        }
    }

}