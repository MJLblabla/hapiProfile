package com.hapi.aop

import android.util.Log

class MonitorConfig {
    /**
     * 单帧绘制超时
     */
    var frameCostIssues = 40
    /**
     * 主线程耗时卡顿
     */
    var blockCostIssue = 2000

    /**
     * 回调 （默认回调 log
     */
    var issureCallBack:IssureCallBack = object : IssureCallBack {
        override fun onIssure(issure: Issure) {
            Log.d("IssureCallBack","msg  ${issure.msg}  availMemory ${issure.availMemory} " +
                    "totalMemory ${issure.totalMemory}   foregroundPageName  ${issure.foregroundPageName}  cpuRate" +
                    "${issure.cpuRate}"

            )
            issure.methodBeats?.forEachIndexed { index, methodBeat ->
                Log.d("IssureCallBack", "top  ${index}  ${methodBeat.sign}  cost ${methodBeat.cost}")
            }
        }

    }

}