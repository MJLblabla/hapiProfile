package com.hapi.aop.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.hapi.aop.ActivityCollection;
import com.hapi.aop.HapiMonitorPlugin;
import com.hapi.aop.R;
import com.hapi.aopbeat.Issure;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationIssure {

    private static int id=1;

    public static void send(final Issure is){

        IssureWrap issure = new IssureWrap();
        issure.setAvailMemory(is.availMemory);
        issure.setCpuRate(is.cpuRate);
        issure.setForegroundPageName(is.foregroundPageName);
        issure.setMethodBeats(is.methodBeats);
        issure.setMsg(is.msg);
        issure.setTotalMemory(is.totalMemory);

        Context context = HapiMonitorPlugin.INSTANCE.getContext().getApplicationContext();
        Intent intent ;
        id++;
        if(ActivityCollection.INSTANCE.getCurrentActivity()!=null){
            intent = new Intent(ActivityCollection.INSTANCE.getCurrentActivity(), IssureActivity.class);
        }else {
            intent = new Intent(context, IssureActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        intent.putExtra("issure",issure);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
// 1. 创建一个通知(必须设置channelId)

            String channelId = "ChannelId"+id; // 通知渠道
            Notification notification = new Notification.Builder(context)
                    .setChannelId(channelId)
                     .setSmallIcon(R.drawable.block)
                    .setContentTitle("卡顿")
                    .setContentIntent(pendingIntent)
                    .setContentText(issure.getMsg())
                    .build();
// 2. 获取系统的通知管理器(必须设置channelId)
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "hapi",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
// 3. 发送通知(Notification与NotificationManager的channelId必须对应)
            Log.d("NotificationIssure","id "+id);
            notificationManager.notify(id, notification);
        }else {
// 创建通知(标题、内容、图标)
            Notification notification = new Notification.Builder(context)
                    .setContentTitle("卡顿")
                    .setContentText(issure.getMsg())
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.block)
                    .build();
// 创建通知管理器
            NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
// 发送通知
            manager.notify(id, notification);
        }
    }
}
