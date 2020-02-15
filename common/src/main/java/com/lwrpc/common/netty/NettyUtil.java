package com.lwrpc.common.netty;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class NettyUtil {
    private static final AttributeKey<String> ATTR_KEY_READER_TIME = AttributeKey.valueOf("readerTime");
    public static void updateReaderTime(Channel channel, Long time) {
        channel.attr(ATTR_KEY_READER_TIME).set(time.toString());
    }
    public static Long getReaderTime(Channel channel) {
        String value = channel.attr(ATTR_KEY_READER_TIME).get();
        if (value != null) {
            return Long.valueOf(value);
        }
        return null;
    }
}
