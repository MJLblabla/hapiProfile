package com.hapi.aop

import kotlinx.coroutines.*

/**
 * 每次取system时间太耗费，每5好
 */
object LoopTimer {
    private var jobTime: Job? = null
    var time = 0
        private set

    var timerObsever = 0

    /**
     * 记录需要用到时间的插件 如果没有人用到 那我也没必要计时了
     */
    fun addTimerObsever(){
        timerObsever++
        if(timerObsever==1){
            startTimer()
        }
    }

    fun removeTimerObsever(){
        timerObsever--
        if(timerObsever==0){
            stop()
        }
    }

    var isStart = false
    fun startTimer() {
        if(timerObsever<=0){
            return
        }
        isStart = true
        time = 0
        jobTime?.cancel()
        jobTime = GlobalScope.launch(Dispatchers.Default) {
            repeat(Int.MAX_VALUE) {
                time += 5
                delay(5)
            }
        }
    }

    fun stop() {
        isStart = false
        jobTime?.cancel()
        jobTime = null
    }

    fun reset(){
        time = 0
    }
}