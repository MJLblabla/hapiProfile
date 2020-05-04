package com.hapi.aop

import android.util.Log
import com.hapi.aop.ui.NotificationIssure

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
            Log.d("IssureCallBack","msg  ${issure.msg}  availMemory ${issure.availMemory} " +
                    "totalMemory ${issure.totalMemory}   foregroundPageName  ${issure.foregroundPageName}  cpuRate" +
                    "${issure.cpuRate}"

            )
            NotificationIssure.send(issure)
            issure.methodBeats?.forEachIndexed { index, methodBeat ->
                Log.d("IssureCallBack", "top  ${index}  ${methodBeat.sign}  cost ${methodBeat.cost}")
            }
        }

    }

}