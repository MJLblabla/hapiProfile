package com.hapi.aopbeat;

import java.util.List;

public interface BeatAdapter {

    public boolean isMainStart();
    public long getMainThreadId();
    public int getCurrentTime();
    public void issure(List<Beat> beats, int maxTop,String msg);

}
