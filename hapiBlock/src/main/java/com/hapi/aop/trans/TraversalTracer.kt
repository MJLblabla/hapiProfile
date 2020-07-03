package com.hapi.aop.trans

import android.os.SystemClock
import android.view.Choreographer
import com.hapi.aop.BlockTranceManager
import com.hapi.aop.LoopTimer
import com.hapi.aopbeat.MethodBeatMonitorJava
import java.lang.reflect.Method

/**
 * 绘制耗时检测
 */
class TraversalTracer :ITracer(){

    private val TAG = "TraselTracer"
    private var lastframeTimeNanos = -1L

    private val CALLBACK_TRAVERSAL = 2
    private val ADD_CALLBACK = "addCallbackLocked"
    private var frameIntervalNanos: Long = 16666666
    private var callbackQueueLock: Any? = null
    private var callbackQueues: Array<Any>? = null
    private val choreographer = Choreographer.getInstance()
    private var addTraversalQueue: Method? = null

    private var startTime = 0


    init {
      //  callbackQueueLock = reflectObject<Any>(choreographer, "mLock")
        callbackQueues = reflectObject<Array<Any>>(choreographer, "mCallbackQueues")
        addTraversalQueue = reflectChoreographerMethod(
            callbackQueues!![CALLBACK_TRAVERSAL],
            ADD_CALLBACK,
            Long::class.java,
            Any::class.java,
            Any::class.java
        )
        frameIntervalNanos = reflectObject<Long>(choreographer, "mFrameIntervalNanos")?:16666666

    }

    override fun loopStart() {
        super.loopStart()
        startTime = LoopTimer.time
    }

    override fun loopStop() {
        super.loopStop()
        if(isTraversal){
            isTraversal = false
            if(isStart){
                val timeSpan = LoopTimer.time - startTime
                if(timeSpan >  BlockTranceManager.mMonitorConfig.frameCostIssues){
                    MethodBeatMonitorJava.issue("单帧耗时过大 ${timeSpan}",timeSpan)
                }
                addCallbackQueues()
            }
        }
    }

    override fun start(){
       super.start()
       addCallbackQueues()
    }
    override fun stop(){
        super.stop()
    }

    private var isTraversal = false

    private val callback = Runnable {
        isTraversal = true
    }

    private fun addCallbackQueues(){
        addTraversalQueue?.invoke(
            callbackQueues!![CALLBACK_TRAVERSAL],
            SystemClock.uptimeMillis(),
            callback,
            null
        )
    }

    private fun <T> reflectObject(instance: Any, name: String): T? {
        try {
            val field = instance.javaClass.getDeclaredField(name)
            field.isAccessible = true
            return field[instance] as T
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    private fun reflectChoreographerMethod(
        instance: Any,
        name: String,
        vararg argTypes: Class<*>
    ): Method? {
        try {
            val method =
                instance.javaClass.getDeclaredMethod(name, *argTypes)
            method.isAccessible = true
            return method
        } catch (e: Exception) {
           e.printStackTrace()
        }
        return null
    }

}