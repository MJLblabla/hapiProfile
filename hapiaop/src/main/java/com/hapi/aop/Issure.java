package com.hapi.aop;

import java.util.List;

public class Issure {
    /**
     * 提示信息
     */
    public String msg;


    /**
     * 可用内存
     */
    public  long availMemory ;
    /**
     *
     */
    public  long totalMemory ;//= DeviceUtil.getTotalMemory(HapiPlugin.context)

    /**
     * cup
     */
    public  double cpuRate ;//= DeviceUtil.getAppCpuRate()

    /**
     * 页面名称
     */
    public String foregroundPageName;
    /**
     * 函数调用链
     */
    public List<Beat> methodBeats;
}
