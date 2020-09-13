package com.example.redditviewer;

import java.io.Serializable;

@SuppressWarnings("serial")
public class transitionData implements Serializable {
    private String url;
    private int delay;
    private int limit;
    public transitionData(String url, int delay, int limit){
        this.url = url;
        this.delay = delay;
        this.limit=limit;
    }

    public String getURL(){
        return this.url;
    }
    public int getDelay(){
        return this.delay;
    }
    public int getLimit(){
        return this.limit;
    }
}
