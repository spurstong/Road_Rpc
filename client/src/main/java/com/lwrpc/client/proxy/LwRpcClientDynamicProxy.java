package com.lwrpc.client.proxy;

import com.lwrpc.client.netty.NettyClient;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class LwRpcClientDynamicProxy<T> implements InvocationHandler {
    private Class<T> clazz;
    public LwRpcClientDynamicProxy(Class<T> clazz) throws Exception {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LwRequest lwRequest = new LwRequest();
        String requestId = UUID.randomUUID().toString();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        lwRequest.setRequestId(requestId);
        lwRequest.setClassName(className);
        lwRequest.setMethodName(methodName);
        lwRequest.setParameterTypes(parameterTypes);
        lwRequest.setParameters(args);
        NettyClient nettyClient = new NettyClient("127.0.0.1", 8888);
        log.info("开始连接服务器端:{}", new Date());
        LwResponse send = nettyClient.send(lwRequest);
        log.info("请求后返回的结果:{}", send.getResult());
        return send.getResult();
    }
}
