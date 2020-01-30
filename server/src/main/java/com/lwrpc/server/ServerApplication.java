package com.lwrpc.server;

import com.lwrpc.server.netty.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.net.InetAddress;

@SpringBootApplication
public class ServerApplication  {

    public static void main(String[] args)  {

        SpringApplication.run(ServerApplication.class, args);
    }


}
