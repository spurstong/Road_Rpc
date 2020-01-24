package com.lwrpc.client.future;

import com.lwrpc.common.msg.LwResponse;

//自定义实现Future
public class DefaultFuture {
    private LwResponse lwResponse;
    private volatile boolean isSucceed = false;
    private final Object object = new Object();

    public LwResponse getLwResponse(int timeout) {
        synchronized (object) {
            while (!isSucceed) {
                try {
                    object.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return lwResponse;
        }
    }
    public void setLwResponse(LwResponse lwResponse) {
        if (isSucceed)
            return;
        synchronized (object) {
            this.lwResponse = lwResponse;
            this.isSucceed = true;
            object.notify();
        }
    }
}
