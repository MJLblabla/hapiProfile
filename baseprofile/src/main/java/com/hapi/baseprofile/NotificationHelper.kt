package com.hapi.baseprofile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private var id = 1

    fun send(issure: NotificationAble) {
        val context = ActivityCollection.appContext
        id++
        var pendingIntent:PendingIntent?=null
        issure.getIntent()?.let {
             pendingIntent = PendingIntent.getActivity(
                context, id, issure.getIntent(), PendingIntent.FLAG_UPDATE_CURRENT
            )

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
// 1. 创建一个通知(必须设置channelId)
            val channelId = "ChannelId$id" // 通知渠道
            val notification = NotificationCompat.Builder(context)
                .setChannelId(channelId)
                .setSmallIcon(R.drawable.block)
                .setContentTitle(issure.getTittle())
                .apply {
                    if(pendingIntent!=null){
                        setContentIntent(pendingIntent)
                    }
                }
                .setStyle (NotificationCompat.BigTextStyle().bigText(issure.getContent()))
                .build()
            // 2. 获取系统的通知管理器(必须设置channelId)
            val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

            val notification = NotificationCompat.Builder(context)
                .setContentTitle(issure.getTittle())

                .setStyle (NotificationCompat.BigTextStyle().bigText(issure.getContent()))

                .apply {
                    if(pendingIntent!=null){
                        setContentIntent(pendingIntent)
                    }
                }
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.block)
                .build()
            // 创建通知管理器
            val manager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 发送通知
            manager.notify(id, notification)
        }
    }
}