package com.lwrpc.client.async;

import com.lwrpc.client.connect.LwRequestManager;
import com.lwrpc.common.msg.LwResponse;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class LwRequestPool {
    private final ConcurrentHashMap<String, Promise<LwResponse>> requestPool = new ConcurrentHashMap<>();
    public void submitRequest(String requestId, EventExecutor executor) {
        requestPool.put(requestId, new DefaultPromise<>(executor));
    }

    public LwResponse fetchResponse(String requestId) throws Exception {
        Promise<LwResponse> promise = requestPool.get(requestId);
        System.out.println("promise结果：" + promise);
        if (promise == null)
            return null;
        LwResponse response = promise.get(10, TimeUnit.SECONDS);
        requestPool.remove(requestId);

        LwRequestManager.destroyChannelHolder(requestId);
        return response;
    }

    public void notifyRequest(String requestId, LwResponse response) {
        Promise<LwResponse> promise = requestPool.get(requestId);
        if (promise != null) {
            promise.setSuccess(response);
        }
    }
}
