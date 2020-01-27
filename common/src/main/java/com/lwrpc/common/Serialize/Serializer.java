package com.lwrpc.common.Serialize;

import java.io.IOException;

//序列化接口
public interface Serializer {
    //将java对象转换为二进制
    byte[] serialize(Object object) throws IOException;
    //将二进制文件转化为java对象
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
