package com.hapi.aop

import android.app.Application
import android.content.Context
import android.os.Looper
import android.util.Log
import com.hapi.aop.trans.BlockTracer
import com.hapi.aop.trans.FpsTracer
import com.hapi.aop.trans.TraversalTracer
import com.hapi.aop.util.DeviceUtil
import com.hapi.aopbeat.Beat
import com.hapi.aopbeat.BeatAdapter
import com.hapi.aopbeat.Issure
import com.hapi.aopbeat.MethodBeatMonitorJava
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*


/**
 * 插件开启，配置入口
 */
object HapiMonitorPlugin {


    var context: Context? = null
        private set
    val mBlockTracer = BlockTracer()
    val mFpsTracer = FpsTracer()
    val mTraversalTracer = TraversalTracer()

    var mMonitorConfig = MonitorConfig()
        private set

    private const val issureTop = 30
    init {
        LooperMonitor.start()
        MethodBeatMonitorJava.mBeatAdapter = object : BeatAdapter {
            override fun getCurrentTime(): Int {
              return  LoopTimer.time
            }

            override fun getMainThreadId(): Long {
               return Looper.getMainLooper().thread.id
            }

            override fun issure(beats: MutableList<Beat>,maxTop:Int, msg:String) {

                GlobalScope.launch(Dispatchers.Main) {

                   val res= async< LinkedList<Beat> ?> {
                        val beatsClone =
                            LinkedList<Beat>()
                        var maxCost = 0
                        var i = beats.size - 1
                        while (i < 0) {
                            beatsClone.add(beats[i])
                            if (beats[i].cost > maxCost) {
                                maxCost = beats[i].cost
                            }
                            i--
                        }

                        if (MethodBeatMonitorJava.mBeatAdapter == null ||
                            beatsClone.isEmpty()
                            ||maxCost < maxTop *0.5
                                ) {
                            Log.d("mjl","maxCost"+maxCost)
                        }else{
                            var methodName = ""
                            var cost = 0
                            val iterator =
                                beatsClone.iterator()
                            val beatTemp =
                                LinkedList<Beat>()

                            while (iterator.hasNext()) {
                                val i = iterator.next()
                                val index = i.sign.lastIndexOf('(')
                                if (index <= 0) {
                                    continue
                                }
                                val itemMethodNameArrayButParam2 = i.sign.substring(0, index)
                                val itemMethodNameArrayParam = i.sign.substring(index)
                                val itemMethodNameArray =
                                    itemMethodNameArrayButParam2.split("\\.".toRegex()).toTypedArray()
                                if (itemMethodNameArray.size < 2) {
                                    continue
                                }
                                val name =
                                    itemMethodNameArray[itemMethodNameArray.size - 1] + itemMethodNameArrayParam
                                if (name == methodName && i.cost == cost) {
                                    iterator.remove()
                                } else {
                                    beatTemp.add(i)
                                    if (beatTemp.size > issureTop) {
                                        break
                                    }
                                }
                                methodName = name
                                cost = i.cost
                            }
                        }

                       beatsClone
                    }

                    res?.await()?.let { beat->

                        HapiMonitorPlugin.mMonitorConfig.issureCallBack?.let {
                            val issure = Issure()
                            issure.msg = msg
                            issure.availMemory = DeviceUtil.getMemFree(HapiMonitorPlugin.context)
                            issure.totalMemory = DeviceUtil.getTotalMemory(HapiMonitorPlugin.context)
                            issure.cpuRate = DeviceUtil.getAppCpuRate()
                            issure.foregroundPageName = ActivityCollection.currentActivity?.localClassName
                            issure.methodBeats = beat
                            it.onIssure(issure)
                        }
                    }
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