package com.hapi.aop

import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import java.lang.reflect.Method

object ChoreographerMonitor : IMonitor {

    private var isForeground = true
    private var lastframeTimeNanos = -1L
    private var lastSecFrameTimeNanos = -1L
    private var fpsCount = 0

    private val TAG = "ChoreographerMonitor"
    private val choreographer by lazy { Choreographer.getInstance() }
    private val fpsFrameCallback = Choreographer.FrameCallback {
        onFrame(it)
    }

    private fun onFrame(frameTimeNanos: Long) {
        if (lastSecFrameTimeNanos <= 0) {
            fpsCount = 0
            lastSecFrameTimeNanos= frameTimeNanos
        }

        fpsCount++

        lastframeTimeNanos = frameTimeNanos;
        isFpsing = true
        if (isStart) {
            choreographer.postFrameCallback(fpsFrameCallback)
        }
    }

    val fpsListerListers = ArrayList<FpsLister>()


    interface FpsLister {
        /**
         * 当前帧耗时
         */
        fun onFrame(currentFrameCost: Long)

        /**
         * 上一秒帧率
         */
        fun onLastSecCost(frameRate: Int)
    }

    init {
        LooperMonitor.registerLoopListener(object : LooperMonitor.LoopListener {
            override fun dispatchMsgStart() {
            }

            override fun dispatchMsgStop() {
                if (isFpsing) {
                    val current =System.nanoTime();
                    val cost = if (lastframeTimeNanos == 0L) {
                        0
                    } else {
                        val costthis= (  (current - lastframeTimeNanos) /1000000)
                        if (lastSecFrameTimeNanos >= 0 && (current - lastSecFrameTimeNanos)/1000000 > 1000) {
                            fpsListerListers.forEach {
                                it.onLastSecCost(fpsCount)
                            }
                            resetFrameCount()
                        }
                        costthis
                    }

                    isFpsing=false
                    fpsListerListers.forEach {
                        it.onFrame(cost)
                    }
                }
            }
        }, true);
    }

    private fun resetFrameCount() {
        lastSecFrameTimeNanos = -1L
        fpsCount = 0
    }

    private var isStart = false
    private var isFpsing = false

    override fun start() {
        isStart = true
        choreographer.postFrameCallback(fpsFrameCallback)
    }

    override fun stop() {
        resetFrameCount()
        choreographer.removeFrameCallback(fpsFrameCallback)
        isStart = false
    }

    override fun onForeground(isForeground: Boolean) {
        this.isForeground = isForeground
        if (!isForeground) {
            resetFrameCount()
        }
    }


}