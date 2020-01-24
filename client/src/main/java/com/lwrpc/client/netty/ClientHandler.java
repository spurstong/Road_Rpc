package com.lwrpc.client.netty;

import com.lwrpc.client.future.DefaultFuture;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ClientHandler extends ChannelDuplexHandler {
    private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof LwResponse) {
            LwResponse response = (LwResponse)msg;
            DefaultFuture defaultFuture = futureMap.get(response.getRequestId());
            defaultFuture.setLwResponse(response);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("成功获取:{}", msg);
        if (msg instanceof LwRequest) {
            LwRequest request = (LwRequest) msg;
            log.info("客户端发送请求:{}", ((LwRequest) msg).getRequestId());
            futureMap.putIfAbsent(request.getRequestId(), new DefaultFuture());
            log.info("客户端发送后放入的map:{}", futureMap);
        }
        super.write(ctx, msg, promise);
    }

    public LwResponse getLwRpcResponse(String requestId) {
        try {
            DefaultFuture future = futureMap.get(requestId);
            log.info("客户端获取的future:{}", future);
            return future.getLwResponse(10);
        } finally {
            futureMap.remove(requestId);
        }
    }
}
