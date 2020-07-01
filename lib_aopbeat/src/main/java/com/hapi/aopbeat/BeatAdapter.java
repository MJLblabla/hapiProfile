package com.hapi.hapiplugin.beat;

import java.util.LinkedList;
import java.util.List;

public interface BeatAdapter {

    public boolean isMainStart();
    public long getMainThreadId();
    public int getCurrentTime();
    public void issure(List<Beat> beats,String msg);

}
