package com.hapi.aoptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Choreographer

class MainActivity : AppCompatActivity() {

    var last = 0L
    private val fpsFrameCallback = Choreographer.FrameCallback {
        Log.d("fpsFrameCallback", "请求一阵   "+(it/1000000-last))
        last = it/1000000;

        onFrame(it)
    }

    private val choreographer by lazy { Choreographer.getInstance() }
    private fun onFrame(frameTimeNanos:Long){

            choreographer.postFrameCallback(fpsFrameCallback)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        choreographer.postFrameCallback(fpsFrameCallback)
    }
}
