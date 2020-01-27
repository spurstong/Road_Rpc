package com.lwrpc.registry.ZK;

import com.lwrpc.registry.heartbeat.HeartBeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

//注册中心心跳检测服务器，通过查看心跳来查看各个服务器是否存活

@Component
public class ZKServer {
    @Value("${zkserver.address}")
    private String addressStr;
    public void start() {

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        ConcurrentHashMap<String, String> channelIdUrlMap = new ConcurrentHashMap<>();
        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringEncoder())
                                    .addLast(new StringDecoder())
                                    .addLast(new IdleStateHandler(0, 0, 60))
                                    .addLast(new HeartBeatHandler(channelIdUrlMap));
                        }
                    });
            String[] strs = addressStr.split(":");
            String hostname = strs[0];
            Integer port = Integer.valueOf(strs[1]);
            ChannelFuture future = bootstrap.bind(hostname, port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
