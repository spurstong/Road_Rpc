package com.lwrpc.registry.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartBeatClient {
    private HeartBeatClient() {

    }
    public static void send(String url, String hostname, Integer port) {
        try {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture future = bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel){
                            socketChannel.pipeline().addLast(new StringEncoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) {
                                            System.out.println("由于不活跃次数在5分钟内超过2次,链接被关闭");
                                        }
                                    });
                        }
                    }).connect(hostname, port).sync();
            log.info("心跳客户端连接成功!");

            //至少延迟1分钟后进行发送
            service.scheduleAtFixedRate(() -> {
                if (future.channel().isActive()) {
                    int time = new Random().nextInt(5);
                    log.info("本次定时任务获取的随机数:{}", time);
                    if (time > 3) {
                        log.info("发送本地地址到注册中心：{}", url);
                        future.channel().writeAndFlush(url);
                    }
                }
            }, 60, 60, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
