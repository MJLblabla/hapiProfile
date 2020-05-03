package com.hapi.aop

import com.hapi.aop.trans.BlockTracer
import com.hapi.aop.trans.FpsTracer
import com.hapi.aop.trans.TraversalTracer

object HapiPlugin {


    init {
        LooperMonitor.start()
    }

    fun startAllMonitor(){
        BlockTracer.start()
        FpsTracer.start()
        TraversalTracer.start()
    }




}