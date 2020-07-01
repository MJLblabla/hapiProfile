package com.hapi.hapiplugin.beat;


import java.io.Serializable;

public class Beat implements Serializable {
    public int cost;
    public String sign="";

    public int id;

    public Beat(){}
    public Beat(int cost, String sign) {
        this.cost = cost;
        this.sign = sign;
    }

}
