package com.hapi.aopbeat;


import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 打点器
 */
public class MethodBeatMonitorJava {

    private final String Tag = "MethodBeatMonitor";

    /**
     * 过滤短耗时
     */
    private static int minCostFilter = 5;
    private static int methodDeep = 0;
    private static int maxDeep = 100000;
    private static int issureTop = 30;
    private static LinkedList<Beat> beats = new LinkedList<Beat>();
    public static BeatAdapter mBeatAdapter;


    public static void dispatchMsgStart() {
        methodDeep = 0;
        beats.clear();
    }


    public static boolean checkDeep() {
        if (mBeatAdapter == null) {
            return false;
        }
        if (Thread.currentThread().getId() != mBeatAdapter.getMainThreadId()) {
            return false;
        }
        if (!mBeatAdapter.isMainStart()) {
            return false;
        }
        return methodDeep < maxDeep;
    }

    /**
     * 方法结束 打点
     * @param beat
     */
    public static void addBeat(Beat beat) {
        methodDeep++;
        beats.addFirst(beat);

    }

    public static int getTime() {
        if (mBeatAdapter == null) {
            return 0;
        }
        return mBeatAdapter.getCurrentTime();
    }

    public static int getMinCostFilter() {
        return minCostFilter;
    }

    /**
     * 取出打点记录 回调上报
     * @param msg
     */
    public static void issue(String msg,int maxTop) {
        mBeatAdapter.issure(beats,maxTop,msg);
    }


}
