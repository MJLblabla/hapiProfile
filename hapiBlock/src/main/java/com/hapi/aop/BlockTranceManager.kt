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
import com.hapi.baseprofile.ActivityCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*


/**
 * 插件开启，配置入口
 */
object BlockTranceManager {


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

            override fun issure(bt: MutableList<Beat>,maxTop:Int, msg:String) {

                val beatsClone =
                    LinkedList<Beat>()
                beatsClone.addAll(bt)
                GlobalScope.launch(Dispatchers.Main) {

                   val res= async< LinkedList<Beat> ?> {
                       val beatTemp =
                           LinkedList<Beat>()


                       if(beatsClone.isEmpty()){
                            Log.d("mjl","beats is empty")
                       }else{
                           var maxCost = 0

                           beatsClone.forEach {
                               if(it.cost>maxCost){
                                   maxCost=it.cost
                               }
                           }

                           if (
                               beatsClone.isEmpty()
                               ||maxCost < maxTop *0.5
                           ) {
                               Log.d("mjl","maxCost"+maxCost)
                           }else{
                               beatTemp.addAll(beatsClone)


//                               var methodName = ""
//                               var cost = 0
//                               val iterator =
//                                   beatsClone.iterator()
//
//
//                               while (iterator.hasNext()) {
//                                   val i = iterator.next()
//                                   val index = i.sign.lastIndexOf('(')
//                                   if (index <= 0) {
//                                       continue
//                                   }
//                                   val itemMethodNameArrayButParam2 = i.sign.substring(0, index)
//                                   val itemMethodNameArrayParam = i.sign.substring(index)
//                                   val itemMethodNameArray =
//                                       itemMethodNameArrayButParam2.split("\\.".toRegex()).toTypedArray()
//                                   if (itemMethodNameArray.size < 2) {
//                                       continue
//                                   }
//                                   val name =
//                                       itemMethodNameArray[itemMethodNameArray.size - 1] + itemMethodNameArrayParam
//                                   if (name == methodName && i.cost == cost) {
//                                       Log.d("mjl","过滤删除方法 "+i.sign)
//                                       iterator.remove()
//                                   } else {
//                                       beatTemp.add(i)
//
//                                   }
//                                   methodName = name
//                                   cost = i.cost
//                               }
                           }
                       }


                       beatTemp
                    }

                    res?.await()?.let { beat->
                        if(beat.isEmpty()){
                            return@let
                        }
                        BlockTranceManager.mMonitorConfig.issureCallBack?.let {
                            val issure = Issure()
                            issure.msg = msg
                            issure.availMemory = DeviceUtil.getMemFree(BlockTranceManager.context)
                            issure.totalMemory = DeviceUtil.getTotalMemory(BlockTranceManager.context)
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
        ActivityCollection.init(application)
    }

    fun startAllMonitor() {
        mBlockTracer.start()
        mFpsTracer.start()
        mTraversalTracer.start()
    }


}