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
        LinkedList<Beat> beatsClone = new LinkedList<Beat>();

        int maxCost=0;
        for (int i=beats.size()-1;i<0;i--){
            beatsClone.add(beats.get(i));
           if(beats.get(i).cost>maxCost){
               maxCost=beats.get(i).cost;
           }
        }
        beatsClone.addAll(beats);

        if(mBeatAdapter==null){
            return;
        }

        if(beatsClone.isEmpty()){
            return;
        }

//        beatsClone.sort(new Comparator<Beat>() {
//            @Override
//            public int compare(Beat o1, Beat o2) {
//                return o2.cost - o1.cost;
//            }
//        });

        if(maxCost<maxTop){
            return;
        }

        String methodName = "";
        int cost = 0;

        Iterator<Beat> iterator = (Iterator<Beat>) beatsClone.iterator();
        LinkedList<Beat> beatTemp = new LinkedList<Beat>();

        while (iterator.hasNext()) {
            Beat i = iterator.next();

            int index =i.sign.lastIndexOf('(');
            if(index<=0){
                continue;
            }

            String itemMethodNameArrayButParam2=i.sign.substring(0,index);
            String itemMethodNameArrayParam = i.sign.substring(index);

            String[] itemMethodNameArray = itemMethodNameArrayButParam2.split("\\.");
            if(itemMethodNameArray.length<2){
                continue;
            }

            String name = itemMethodNameArray[itemMethodNameArray.length - 1] +itemMethodNameArrayParam;
            if (name .equals(methodName )&& i.cost == cost) {
                iterator.remove();
            } else {
                beatTemp.add(i);
                if (beatTemp.size() > issureTop) {
                    break;
                }
            }
            methodName = name;
            cost = i.cost;

        }
        mBeatAdapter.issure(beatTemp,msg);
    }


}
