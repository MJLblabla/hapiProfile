package com.hapi.aop

import android.app.Application
import android.content.Context
import android.os.Looper
import com.hapi.aop.trans.BlockTracer
import com.hapi.aop.trans.FpsTracer
import com.hapi.aop.trans.TraversalTracer
import com.hapi.aop.util.DeviceUtil
import com.hapi.hapiplugin.beat.Beat
import com.hapi.hapiplugin.beat.BeatAdapter
import com.hapi.hapiplugin.beat.Issure
import com.hapi.hapiplugin.beat.MethodBeatMonitorJava

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
        MethodBeatMonitorJava.mBeatAdapter = object : BeatAdapter{
            override fun getCurrentTime(): Int {
              return  LoopTimer.time
            }

            override fun getMainThreadId(): Long {
               return Looper.getMainLooper().thread.id
            }

            override fun issure(p0: MutableList<Beat>?,msg:String) {
                HapiMonitorPlugin.mMonitorConfig.issureCallBack?.let {
                    val issure = Issure()
                    issure.msg = msg
                    issure.availMemory = DeviceUtil.getMemFree(HapiMonitorPlugin.context)
                    issure.totalMemory = DeviceUtil.getTotalMemory(HapiMonitorPlugin.context)
                    issure.cpuRate = DeviceUtil.getAppCpuRate()
                    issure.foregroundPageName = ActivityCollection.currentActivity?.localClassName
                    issure.methodBeats = p0
                    it.onIssure(issure)
                }
            }

            override fun isMainStart(): Boolean {
                    return LoopTimer.isStart
            }

        }
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