package com.hapi.aop.trans

import android.util.Log
import android.view.Choreographer

/**
 * 帧率检查
 */
class FpsTracer : ITracer(false){

    private val TAG="FpsTracer"
    private val choreographer = Choreographer.getInstance()
    private val fpsFrameCallback = Choreographer.FrameCallback {
        onFrame(it)
    }

    private var lastSecTime = 0L
    private var fps = -1

    private fun onFrame(it: Long) {
        if(lastSecTime==0L){
            lastSecTime = it
        }
        fps++

        if( (it - lastSecTime)/1000000 > 1000) {
                Log.d(TAG,"fps ${fps}")
            fps=-1
            lastSecTime = it
        }
        if(isStart){
            choreographer.postFrameCallback(fpsFrameCallback)
        }
    }




    override fun start(){
         super.start()
        choreographer.postFrameCallback(fpsFrameCallback)
    }

    override fun stop(){
        super.stop()
        fps=0
        lastSecTime=0
        choreographer.removeFrameCallback(fpsFrameCallback)
    }

}