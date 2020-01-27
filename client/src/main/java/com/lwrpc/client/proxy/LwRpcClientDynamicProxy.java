package com.lwrpc.client.proxy;

import com.lwrpc.client.netty.NettyClient;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import com.lwrpc.registry.ZK.ZKRegister;
import com.lwrpc.registry.data.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

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
        System.out.println(className);

        URL url= ZKRegister.random(className);
        if (url.getInterfaceName().equals(lwRequest.getClassName())) {
            System.out.println("接口一致");
        }
        lwRequest.setImplClassName(url.getImplClassName());
        System.out.println("返回的url：" + url.getPort());
        NettyClient nettyClient = new NettyClient(url.getHostname(), url.getPort());
        log.info("开始连接服务器端:{}", new Date());
        LwResponse send = nettyClient.send(lwRequest);
        log.info("请求后返回的结果:{}", send.getResult());
        return send.getResult();
    }


}
