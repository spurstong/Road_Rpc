package com.lwrpc.client.netty;

import com.lwrpc.client.future.DefaultFuture;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class NettyClientHandler extends SimpleChannelInboundHandler<LwResponse> {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private LwResponse response = null;

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public LwResponse getResponse() {
        return response;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext cxt, LwResponse lwResponse) throws Exception {
        log.info("收到服务端的信息:{}", lwResponse.getResult());
        this.response = lwResponse;
        this.countDownLatch.countDown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception", cause);
        ctx.close();
    }
}
