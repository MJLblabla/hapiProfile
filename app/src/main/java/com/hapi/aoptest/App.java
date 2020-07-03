package com.hapi.aoptest;

import android.app.Application;

import com.hapi.aop.BlockTranceManager;
import com.hapi.aop.MonitorConfig;
import com.hapi.memotrancer.MemoryTranceManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BlockTranceManager.INSTANCE.init(this, new MonitorConfig());
        BlockTranceManager.INSTANCE.startAllMonitor();
        MemoryTranceManager.INSTANCE.init(this);
        MemoryTranceManager.INSTANCE.startMemoryProfile(1);
    }
}
