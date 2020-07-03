package com.hapi.aop;

public interface IMonitor {

    void start();
    void stop();

    void onForeground(boolean isForeground);

}

