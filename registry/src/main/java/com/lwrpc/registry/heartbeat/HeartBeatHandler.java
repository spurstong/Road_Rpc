package com.lwrpc.registry.heartbeat;

import com.lwrpc.registry.ZK.ZKRegister;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    //维护chnnelId和具体地址的map,当发生变化时对其进行删除
    private static ConcurrentHashMap<String, String> channelUrlMap;
    //活跃次数
    private int inActiveCount = 0;
    //开始计数时间
    private long start;

    public HeartBeatHandler(ConcurrentHashMap<String, String> channelUrlMap) {
        HeartBeatHandler.channelUrlMap = channelUrlMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String url = msg.toString();
        String id = ctx.channel().id().asShortText();
        log.info("收到chnnelId:{}, 发来消息:{}", id, url);
        if (channelUrlMap.get(id) == null) {
            channelUrlMap.put(id, url);
        }
    }

    //如果10秒内没有触发读，就会执行该方法
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent state = (IdleStateEvent)evt;
            if (state.state().equals(IdleState.READER_IDLE)) {
                log.info("读空闲");
            } else if (state.state().equals(IdleState.WRITER_IDLE)) {
                log.info("写空闲");
            }
            //在一定时间内读写空闲才会关闭链接
            else if (state.state().equals(IdleState.ALL_IDLE)) {
                if (++inActiveCount == 1) {
                    start = System.currentTimeMillis();
                }
                int minute = (int)((System.currentTimeMillis() - start) / (60 * 1000)) + 1;
                log.info("第{}次读写都空闲，计时分钟数{}", inActiveCount, minute);
                if (inActiveCount > 2 && minute < 5) {
                    log.info("移除不活跃ip");
                    removeAndClose(ctx);
                } else {
                    if (minute >= 5) {
                        log.info("新周期开始");
                        start = 0;
                        inActiveCount = 0;
                    }
                }
            }
        }
    }

    private void removeAndClose(ChannelHandlerContext ctx) {
        String id = ctx.channel().id().asShortText();
        String url = channelUrlMap.get(id);
        log.info("移除不活跃的节点：{}", id);
        //移除不活跃的节点
        ZKRegister.remove(url);
        channelUrlMap.remove(id);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        removeAndClose(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channel:{}注册", ctx.channel().id().asShortText());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

        log.info("channel:{}注销", ctx.channel().id().asShortText());
    }
}
