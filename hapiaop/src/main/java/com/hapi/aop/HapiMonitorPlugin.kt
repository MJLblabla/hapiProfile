package com.hapi.aop

import android.app.Application
import android.content.Context
import com.hapi.aop.trans.BlockTracer
import com.hapi.aop.trans.FpsTracer
import com.hapi.aop.trans.TraversalTracer

object HapiMonitorPlugin {


    var context: Context? = null
        private set
    val mBlockTracer = BlockTracer()
    val mFpsTracer = FpsTracer()
    val mTraversalTracer = TraversalTracer()

    var mMonitorConfig = MonitorConfig()
        private set

    init {
        LooperMonitor.start()
    }

    fun init( application: Application,config: MonitorConfig = MonitorConfig()) {
        context = application
        this.mMonitorConfig = config
        application.registerActivityLifecycleCallbacks(ActivityCollection.callBack)
    }

    fun startAllMonitor() {
        mBlockTracer.start()
        mFpsTracer.start()
        mTraversalTracer.start()
    }


}