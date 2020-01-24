package com.lwrpc.common.Serialize;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class LwRpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;
    private Serializer serializer;

    public LwRpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4)
            return;
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        Object obj = serializer.deserialize(clazz, data);
        list.add(obj);
    }
}
