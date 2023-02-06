package com.ruiguan.fragments.threadPool;

public interface ThreadCallBack {

    void accomplish();
    void setOk(String ip);
    void setError(String ip);
    void dispose(String ip);

}
