package com.lwrpc.client;

import com.lwrpc.client.proxy.ProxyFactory;
import com.lwrpc.common.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Slf4j
public class ClientApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ClientApplication.class, args);
        HelloService helloService = ProxyFactory.create(HelloService.class);
        String result = helloService.hello("阿走");
        log.info("请求服务结果:{}", result);
    }

}
