package com.hapi.aop

import kotlinx.coroutines.*

object LoopTimer {
    private var jobTime: Job? = null
    var time = 0
        private set

    var timerObsever = 0

    fun addTimerObsever(){
        timerObsever++
    }

    fun removeTimerObsever(){
        timerObsever--
    }

    var isStart = false
    fun startTimer() {
        isStart = true
        time = 0
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
}