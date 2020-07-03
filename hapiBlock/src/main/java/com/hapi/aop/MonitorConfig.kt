package com.hapi.aop

import android.content.Intent
import com.hapi.aop.ui.IssureActivity
import com.hapi.aopbeat.Beat
import com.hapi.aopbeat.Issure
import com.hapi.baseprofile.ActivityCollection
import com.hapi.baseprofile.NotificationAble
import com.hapi.baseprofile.NotificationHelper
import java.util.*
import kotlin.Comparator

class MonitorConfig {
    /**
     * 单帧绘制超时
     */
    var frameCostIssues = 40
    /**
     * 主线程耗时卡顿
     */
    var blockCostIssue = 1700

    /**
     * 回调 （默认回调 log
     */
    var issureCallBack:IssureCallBack = object : IssureCallBack {
        override fun onIssure(issure: Issure) {

//            Log.d("IssureCallBack","msg  ${issure.msg}  availMemory ${issure.availMemory} " +
//                    "totalMemory ${issure.totalMemory}   foregroundPageName  ${issure.foregroundPageName}  cpuRate" +
//                    "${issure.cpuRate}"
//
//            )
//            NotificationIssure.send(issure)
//            issure.methodBeats?.forEachIndexed { index, methodBeat ->
//                Log.d("IssureCallBack", "top  ${index}  ${methodBeat.sign}  cost ${methodBeat.cost}")
//            }
            NotificationHelper.send(object : NotificationAble {
                override fun getTittle(): String {
                return "老铁，你的代码有点卡->>>"
                }

                override fun getContent(): String {
                   return issure.msg
                }

                override fun getIntent(): Intent? {

                val msg = "msg  ${issure.msg}  " +
                            "\n availMemory ${issure.availMemory} " +
                            " \n totalMemory ${issure.totalMemory} " +
                            "\n foregroundPageName  ${issure.foregroundPageName}" +
                            "  cpuRate ${issure.cpuRate} \n\n"
                    val sb = StringBuffer()
                    sb.append(msg+"  调用顺序：")



                    issure.methodBeats?.forEachIndexed { index, methodBeat ->
                        sb.append(" ${index}  ${methodBeat.sign}  cost ${methodBeat.cost} \n")

                    }
                    sb.append("耗时排序 : '\n\n\n")

                    issure.methodBeats?.let {
                        Collections.sort<Beat>(it, object : Comparator<Beat> {
                            override fun compare(o1: Beat, o2: Beat): Int {
                                return  o2.cost-o1.cost
                            }
                        })
                    }

                    issure.methodBeats?.forEachIndexed { index, methodBeat ->
                        sb.append(" ${methodBeat.sign}  cost ${methodBeat.cost} \n")
                    }
                    var intent = if (ActivityCollection.currentActivity != null) {
                        Intent(ActivityCollection.currentActivity, IssureActivity::class.java)
                    } else {
                        Intent(ActivityCollection.appContext, IssureActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }
                    intent.putExtra("issure",sb.toString())

                    return intent
                }


            })
        }

    }

}