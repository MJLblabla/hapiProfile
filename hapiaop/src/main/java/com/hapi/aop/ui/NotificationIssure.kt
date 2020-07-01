package com.hapi.aop.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.hapi.aop.ActivityCollection.currentActivity
import com.hapi.aop.HapiMonitorPlugin.context
import com.hapi.aop.R
import com.hapi.aopbeat.Issure

object NotificationIssure {
    private var id = 1
    fun send(issure: Issure) {
        val context = context!!.applicationContext
        val intent: Intent
        id++

        val msg = "msg  ${issure.msg}  " +
                "\n availMemory ${issure.availMemory} " +
                " \n totalMemory ${issure.totalMemory} " +
                "\n foregroundPageName  ${issure.foregroundPageName}" +
                "  cpuRate ${issure.cpuRate} \n\n"
        val sb = StringBuffer()
        sb.append(msg)
        issure.methodBeats?.forEachIndexed { index, methodBeat ->
            sb.append("top  ${index}  ${methodBeat.sign}  cost ${methodBeat.cost} \n")

        }

        if (currentActivity != null) {
            intent = Intent(currentActivity, IssureActivity::class.java)
        } else {
            intent = Intent(context, IssureActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.putExtra("issure", sb.toString())

        val pendingIntent = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
// 1. 创建一个通知(必须设置channelId)
            val channelId = "ChannelId$id" // 通知渠道
            val notification = Notification.Builder(context)
                .setChannelId(channelId)
                .setSmallIcon(R.drawable.block)
                .setContentTitle("卡顿")
                .setContentIntent(pendingIntent)
                .setContentText(issure.msg)
                .build()
            // 2. 获取系统的通知管理器(必须设置channelId)
            val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "hapi",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
            // 3. 发送通知(Notification与NotificationManager的channelId必须对应)
            Log.d("NotificationIssure", "id $id")
            notificationManager.notify(id, notification)
        } else {
// 创建通知(标题、内容、图标)
            val notification = Notification.Builder(context)
                .setContentTitle("卡顿")
                .setContentText(issure.msg)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.block)
                .build()
            // 创建通知管理器
            val manager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 发送通知
            manager.notify(id, notification)
        }
    }
}