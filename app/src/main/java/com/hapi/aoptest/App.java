package com.hapi.aoptest;

import android.app.Application;

import com.hapi.aop.HapiMonitorPlugin;
import com.hapi.aop.MonitorConfig;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HapiMonitorPlugin.INSTANCE.init(this, new MonitorConfig());
        HapiMonitorPlugin.INSTANCE.startAllMonitor();
    }
}
