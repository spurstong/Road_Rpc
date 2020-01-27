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
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private LwResponse response = null;
    private LwRequest request;

    public NettyClientHandler(LwRequest request) {
        this.request = request;
    }


    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public LwResponse getLwResponse() {
        return this.response;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端向客户端发送消息");
        ctx.writeAndFlush(request);
        log.info("客户端请求成功");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        LwResponse lwResponse = (LwResponse) msg;
        log.info("收到服务端的信息:{}", lwResponse.getResult());
        this.response = lwResponse;
        this.countDownLatch.countDown();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
