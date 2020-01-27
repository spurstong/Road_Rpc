package com.lwrpc.client.netty;

import com.lwrpc.common.Serialize.HessianSerializer;
import com.lwrpc.common.Serialize.LwRpcDecoder;
import com.lwrpc.common.Serialize.LwRpcEncoder;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient  {
    private String host;
    private Integer port;
    private LwResponse response;
    private EventLoopGroup group;
    private ChannelFuture future = null;
    private Object obj = new Object();
    private NettyClientHandler nettyClientHandler;
    public NettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }


    public LwResponse send(LwRequest request) throws Exception{
        nettyClientHandler = new NettyClientHandler(request);
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                        pipeline.addLast(new LwRpcEncoder(LwRequest.class, new HessianSerializer()));
                        pipeline.addLast(new LwRpcDecoder(LwResponse.class, new HessianSerializer()));
                        pipeline.addLast(nettyClientHandler);
                    }
                });
        System.out.println("host:" + host);
        future = bootstrap.connect(host, port).sync();
        nettyClientHandler.getCountDownLatch().await();
        this.response = nettyClientHandler.getLwResponse();
        return this.response;
    }

    @PreDestroy
    public void close() {
        group.shutdownGracefully();
        future.channel().closeFuture().syncUninterruptibly();
    }

}

//    private void connect(Bootstrap bootstrap, String host, int port, int retry) {
//        ChannelFuture channelFuture = bootstrap.connect(host, port).addListener(future -> {
//            if (future.isSuccess()) {
//                log.info("连接服务端成功!");
//            } else if (retry == 0) {
//                log.error("重试次数已用完，连接失败");
//            } else {
//                int order = (MAX_RETRY - retry) + 1;
//                //本次重连的间隔
//                int delay = 1 << order;
//                log.error("{}:连接失败，第{}重连", new Date(), order);
//                bootstrap.config().group().schedule(()->connect(bootstrap, host, port, retry-1), delay, TimeUnit.SECONDS);
//
//            }
//        });
//        channel = channelFuture.channel();
//    }

