package com.hapi.aoptest;


public class S {


    public void a(){
        //com.hapi.aoptest.MethodBeatMonitorJav.logS("aa");
        a1();

    }
    public void b(){
        b1();
        //com.hapi.aoptest.MethodBeatMonitorJav.logS("aa");
    }
    public void b1(){
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //com.hapi.aoptest.MethodBeatMonitorJav.logS("aa");
    }
    public void c(){
        //com.hapi.aoptest.MethodBeatMonitorJav.logS("aa");
    }

    public void a1(){
        //com.hapi.aoptest.MethodBeatMonitorJav.logS("aa");
    }
    public void a2(){
        //com.hapi.aoptest.MethodBeatMonitorJav.logS("aa");
    }
}
