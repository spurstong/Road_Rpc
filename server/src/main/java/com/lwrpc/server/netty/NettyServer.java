package com.lwrpc.server.netty;

import com.lwrpc.common.Serialize.*;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import com.lwrpc.common.spi.ExtensionLoader;
import com.lwrpc.registry.heartbeat.HeartBeatClient;
import com.lwrpc.server.beanutil.BeanHolder;
import com.lwrpc.server.beanutil.ServerConfig;
import com.lwrpc.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetAddress;


@Slf4j
@Component
public class NettyServer {
    private EventLoopGroup boss = null;
    private EventLoopGroup worker = null;
    @Value("${server.addressPort}")
    private Integer port;
    @Value("${zkserver.address}")
    private String ZkServerAddress;
    @Autowired
    private ServerHandler serverHandler;

    private ExtensionLoader extensionLoader;
    @Autowired
    private BeanHolder beanHolder;
    public void start() throws Exception {
        log.info("成功");
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        extensionLoader = beanHolder.getBean(ExtensionLoader.class);
        Serializer hessianSerializer = (Serializer)extensionLoader.getServiceExtension(Serializer.class);
        System.out.println("hession:" + hessianSerializer);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                            //200秒没有向客户端发送消息或接收消息就关闭通道
                            pipeline.addLast(new IdleStateHandler(0, 0, 200));
                            pipeline.addLast(new LwRpcEncoder(LwResponse.class, hessianSerializer));
                            pipeline.addLast(new LwRpcDecoder(LwRequest.class, hessianSerializer));
                            pipeline.addLast(serverHandler);
                        }
                    });
            String hostAddress = "127.0.0.1";

            ChannelFuture future = serverBootstrap.bind(hostAddress, port).sync();
            String[] strs = ZkServerAddress.split(":");
            String zkHostname = strs[0];
            Integer zkPort = Integer.valueOf(strs[1]);
            //发送心跳
            HeartBeatClient.send(hostAddress + ":" + port,zkHostname, zkPort);
            future.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭netty");
    }

}
