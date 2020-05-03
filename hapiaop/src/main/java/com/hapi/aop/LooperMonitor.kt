package com.hapi.aop

import android.os.Looper
import android.util.Log
import android.util.Printer
import com.hapi.aop.util.ReflectUtils
import java.util.*

object LooperMonitor : IMonitor {

    private val TAG = "TAG"
    private val looper by lazy { Looper.getMainLooper() }

    private var isInt = false
    private var isStart = false

    private var mLoopListeners = LinkedList<LoopListener>()

    fun registerLoopListener(loopListener: LoopListener, register: Boolean) {
        if (register) {
            mLoopListeners.add(loopListener)
        } else {
            mLoopListeners.remove(loopListener)
        }
    }

    interface LoopListener {
        fun dispatchMsgStart()
        fun dispatchMsgStop()
    }

    class LooperPrinter(var origin: Printer?) :
        Printer {
        var isHasChecked = false
        var isValid = false
        override fun println(x: String) {
            origin?.println(x)
            if (!isHasChecked) {
                isValid = x[0] == '>' || x[0] == '<'
                isHasChecked = true
            }
            if (isValid) {
                dispatch(x[0] == '>')
            }
        }
    }

    private fun dispatch(isBegin: Boolean) {
        if (!isStart) {
            return
        }

        if (isBegin) {
            LoopTimer.startTimer()
            MethodBeatMonitor.dispatchMsgStart()
            mLoopListeners.forEach {
                it.dispatchMsgStart()
            }
        } else {
            LoopTimer.stop()
            mLoopListeners.forEach {
                it.dispatchMsgStop()
            }
        }
    }

    private var isReflectLoggingError = false


    private fun init() {
        isInt = true
        var originPrinter: Printer? = null
        try {
            if (!isReflectLoggingError) {
                originPrinter = ReflectUtils.get(looper.javaClass, "mLogging", looper)
            }
        } catch (e: Exception) {
            isReflectLoggingError = true
            Log.e(TAG, "[resetPrinter] %s", e)
        }
        looper.setMessageLogging(LooperPrinter(originPrinter));
    }

    override fun start() {
        if (!isInt) {
            init()
        }
        isStart = true
    }

    override fun stop() {
        isStart = false
    }

    override fun onForeground(isForeground: Boolean) {

    }

}