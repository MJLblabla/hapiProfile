package com.hapi.aoptest;

import android.app.Application;

import com.hapi.aop.HapiPlugin;

public class App  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HapiPlugin.INSTANCE.startAllMonitor();
    }
}
