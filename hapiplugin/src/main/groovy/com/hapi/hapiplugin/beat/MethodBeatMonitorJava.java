package com.hapi.aop;

public class MethodBeatMonitorJava {


    public static boolean checkDeep(){
        return MethodBeatMonitor.INSTANCE.checkDeep();
    }
    public static void addBeat(Beat beat){
        MethodBeatMonitor.INSTANCE.addBeat(beat);
    }
    public static int getTime(){
        return LoopTimer.INSTANCE.getTime();
    }
    public static int getMinCostFilter(){
        return MethodBeatMonitor.INSTANCE.getMinCostFilter();
    }


}
