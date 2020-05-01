package com.hapi.aop;

public class MethodBeatMonitorJava {

  public   static void logS(String methodSign){
        MethodBeatMonitor.INSTANCE.logS(methodSign);
    }
 public    static void logE(String methodSign){
        MethodBeatMonitor.INSTANCE.logE(methodSign);
    }
}
