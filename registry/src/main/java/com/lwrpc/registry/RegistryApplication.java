package com.lwrpc.registry;

import com.lwrpc.registry.ZK.ZKServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

@SpringBootApplication
public class RegistryApplication implements ApplicationRunner {
    @Autowired
    private ZKServer zkServer;
    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        zkServer.start();
    }

}
