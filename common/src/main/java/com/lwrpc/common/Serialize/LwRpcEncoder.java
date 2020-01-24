package com.lwrpc.common.Serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class LwRpcEncoder extends MessageToByteEncoder<Object> {
    private Class<?> clazz;
    private Serializer serializer;

    public LwRpcEncoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (clazz.isInstance(in)) {
            byte[] data = serializer.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }

    }

}
