package com.lwrpc.client.netty;

import com.lwrpc.client.async.LwRequestPool;
import com.lwrpc.common.msg.Event;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import com.lwrpc.common.netty.NettyUtil;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<LwResponse> {
    @Autowired
    private LwRequestPool lwRequestPool;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            System.out.println("陈宫");
            IdleStateEvent idleStateEvent = (IdleStateEvent)evt;
            if(idleStateEvent.state() == IdleState.WRITER_IDLE) {
                LwRequest heartBeat = new LwRequest();
                System.out.println("发送心跳消息");
                //设置消息类型为心跳消息
                heartBeat.setEventType(Event.HEART_BEAT);
                ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener)future->{
                    if (!future.isSuccess()) {
                        Long lastReadTime = NettyUtil.getReaderTime(ctx.channel());
                        long now = System.currentTimeMillis();
                        if (lastReadTime != null && now - lastReadTime > Event.HEART_BERT_TIMEOUT *1000) {
                            System.out.println("发送时间超时，进行重试");
                            reconnect();
                        }
                    }
                });
            }
        }
        super.userEventTriggered(ctx, evt);
    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        //如果运行过程中服务端挂了，执行重连操作
//        System.out.println("心跳消息发送服务端异常");
//        EventLoop eventLoop = ctx.channel().eventLoop();
//        eventLoop.schedule(() -> reconnect(), 10L, TimeUnit.SECONDS);
//        super.channelInactive(ctx);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LwResponse response) throws Exception {
        //接收到心跳事件响应，更新上次读时间
        if (response.getEventType() == Event.HEART_BEAT) {
            NettyUtil.updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
        } else {
            lwRequestPool.notifyRequest(response.getRequestId(), response);
        }
    }
    private void reconnect() {
        System.out.println("执行重连操作！");
    }
}
