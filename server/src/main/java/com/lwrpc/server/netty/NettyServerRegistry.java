package com.lwrpc.server.netty;

import com.lwrpc.registry.ZK.ZKRegister;
import com.lwrpc.server.RpcService;
import com.lwrpc.registry.data.URL;
import com.lwrpc.server.beanutil.BeanHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import sun.dc.pr.PRError;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class NettyServerRegistry implements InitializingBean {
    private Map<String, String> serviceMap = null;
    @Value("${server.addressPort}")
    private Integer port;
    @Autowired
    private NettyServer nettyServer;
    @Autowired
    private BeanHolder beanHolder;

    private static ExecutorService executor = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> beans = beanHolder.getBeansWithAnnotation(RpcService.class);
        System.out.println("服务端服务注册：" + beans.size());
        if(beans != null && beans.size() > 0) {
            serviceMap = new HashMap<>(beans.size());
            for (Object o : beans.values()) {
                String implClassName = o.getClass().getCanonicalName();
                RpcService rpcService = o.getClass().getAnnotation(RpcService.class);
                String interfaceName = rpcService.value().getName();
                serviceMap.put(interfaceName, implClassName);
            }
            serviceMap = Collections.unmodifiableMap(serviceMap);
        }

        String hostAddress = "127.0.0.1";
        if (serviceMap != null && serviceMap.size() > 0) {
            log.info("开始进行服务注册");
            System.out.println("开始服务注册");

            for (Map.Entry<String, String> entry : serviceMap.entrySet()) {
                String interfaceName = entry.getKey();
                String implClassName = entry.getValue();
                URL url = new URL(hostAddress, port, interfaceName, implClassName);
                ZKRegister.register(url);
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            }
        }
        nettyServer.start();
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);
//        System.out.println("服务端服务注册：" + beans.size());
//        if(beans != null && beans.size() > 0) {
//            serviceMap = new HashMap<>(beans.size());
//            for (Object o : beans.values()) {
//                String implClassName = o.getClass().getCanonicalName();
//                RpcService rpcService = o.getClass().getAnnotation(RpcService.class);
//                String interfaceName = rpcService.value().getName();
//                serviceMap.put(interfaceName, implClassName);
//            }
//            serviceMap = Collections.unmodifiableMap(serviceMap);
//        }
//    }
}
