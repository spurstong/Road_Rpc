package com.lwrpc.client.proxy;

import com.lwrpc.client.async.LwRequestPool;
import com.lwrpc.client.bean.SpringBeanFactory;
import com.lwrpc.client.connect.LwRequestManager;
import com.lwrpc.common.msg.Event;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import com.lwrpc.registry.ZK.ZKRegister;
import com.lwrpc.registry.data.URL;
import com.lwrpc.registry.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class LwRpcClientDynamicProxy<T> implements InvocationHandler {
    private Class<T> clazz;
    private LwRequestPool lwRequestPool = SpringBeanFactory.getBean(LwRequestPool.class);
    private FileUtil fileUtil = new FileUtil(true);
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
        lwRequest.setEventType(Event.SERVICE);
        System.out.println(className);
        URL url = null;
        List<URL> urls = fileUtil.getServiceUrls(className);


        if (urls == null || urls.size() == 0) {
            log.info("该服务没有缓存");
            List<URL> addresss = ZKRegister.getServiceInfo(className);
            fileUtil.saveServices(className, addresss);
            url = addresss.get(0);

        } else {
            url = urls.get(0);
        }
        lwRequest.setImplClassName(url.getImplClassName());
        System.out.println("返回的url：" + url.getPort());
        log.info("开始连接服务器端:{}", new Date());
        LwRequestManager.send(lwRequest, url);
        LwResponse response = lwRequestPool.fetchResponse(requestId);
        log.info("请求后返回的结果:{}", response.getResult());
        return response.getResult();
    }


}
