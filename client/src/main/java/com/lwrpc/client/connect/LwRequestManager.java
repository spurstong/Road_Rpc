package com.lwrpc.client.connect;

import com.lwrpc.client.bean.SpringBeanFactory;
import com.lwrpc.client.channel.ChannelHolder;
import com.lwrpc.client.netty.NettyClient;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.client.async.LwRequestPool;
import com.lwrpc.registry.data.URL;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;

@Slf4j
public class LwRequestManager {
    private static final ConcurrentHashMap<String, ChannelHolder> channelHolderMap = new ConcurrentHashMap<>();
    private static ExecutorService requestExecutor = new ThreadPoolExecutor(30, 100, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(30),
            new BasicThreadFactory.Builder().namingPattern("request-service-connector-%d").build());

    private static LwRequestPool requestPool = SpringBeanFactory.getBean(LwRequestPool.class);

    public static void send(LwRequest request, URL url) throws Exception{
        String requestId = request.getRequestId();
        CountDownLatch latch = new CountDownLatch(1);
        requestExecutor.execute(new NettyClient(requestId, url, latch));
        latch.await();

        ChannelHolder channelHolder = channelHolderMap.get(requestId);
        channelHolder.getChannel().writeAndFlush(request);
        log.info("客户端发送消息：{}", channelHolder);
    }

    public static void registerChannelHolder(String requestId, ChannelHolder channelHolder) {
        if(StringUtils.isBlank(requestId) || channelHolder == null) {
            return;
        }
        channelHolderMap.put(requestId, channelHolder);
        log.info("注册channelHolder");
        requestPool.submitRequest(requestId, channelHolder.getChannel().eventLoop());
    }

    public static void destroyChannelHolder(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            return;
        }
        ChannelHolder channelHolder = channelHolderMap.remove(requestId);
        channelHolder.getChannel().closeFuture();
        channelHolder.getEventLoopGroup().shutdownGracefully();
    }

}
