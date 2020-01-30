package com.lwrpc.client.netty;

import com.lwrpc.client.async.LwRequestPool;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<LwResponse> {
    @Autowired
    private LwRequestPool lwRequestPool;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LwResponse response) throws Exception {
        lwRequestPool.notifyRequest(response.getRequestId(), response);
    }
}
