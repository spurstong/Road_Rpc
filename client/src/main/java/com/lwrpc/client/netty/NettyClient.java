package com.lwrpc.client.netty;

import com.lwrpc.client.bean.SpringBeanFactory;
import com.lwrpc.client.channel.ChannelHolder;
import com.lwrpc.client.connect.LwRequestManager;
import com.lwrpc.common.Serialize.HessianSerializer;
import com.lwrpc.common.Serialize.LwRpcDecoder;
import com.lwrpc.common.Serialize.LwRpcEncoder;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import com.lwrpc.registry.data.URL;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient implements Runnable  {
    private String requestId;
    private URL url;
    private LwResponse response;
    private CountDownLatch latch;
    private ClientHandler clientHandler;

    public NettyClient(String requestId, URL url, CountDownLatch latch) {
        this.requestId = requestId;
        this.url = url;
        this.latch = latch;
        this.clientHandler = SpringBeanFactory.getBean(ClientHandler.class);
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
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
                        //10秒没发送信息，将IdleStateHandler添加到ChannelPipeline中
                        pipeline.addLast(new IdleStateHandler(0, 10, 0));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                        pipeline.addLast(new LwRpcEncoder(LwRequest.class, new HessianSerializer()));
                        pipeline.addLast(new LwRpcDecoder(LwResponse.class, new HessianSerializer()));
                        pipeline.addLast(clientHandler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(url.getHostname(), url.getPort()).sync();
            //连接成功
            if (future.isSuccess()) {
                ChannelHolder channelHolder = ChannelHolder.builder()
                        .channel(future.channel())
                        .eventLoopGroup(group).build();
                LwRequestManager.registerChannelHolder(requestId, channelHolder);
                latch.countDown();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

