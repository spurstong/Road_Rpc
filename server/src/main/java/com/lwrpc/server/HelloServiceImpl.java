package com.lwrpc.server;

import com.lwrpc.common.service.HelloService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello, " +name;
    }
}
