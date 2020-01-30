package com.lwrpc.client.channel;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelHolder {
    private Channel channel;
    private EventLoopGroup eventLoopGroup;
}
