package com.ruiguan.fragments.threadPool;

public interface MessageInform {
    void serviceCallback(boolean result, String data);
    void sendModuleCallback(boolean result, String pattern);
}
