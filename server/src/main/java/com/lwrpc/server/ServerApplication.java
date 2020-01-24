package com.lwrpc.server;

import com.lwrpc.server.netty.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication implements ApplicationRunner {
    @Autowired
    NettyServer nettyServer;
    public static void main(String[] args)  {

        SpringApplication.run(ServerApplication.class, args);
    }
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        nettyServer.start();
    }

}
