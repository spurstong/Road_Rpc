package com.lwrpc.common.Serialize;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JSONSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        return JSON.parseObject(bytes, clazz);
    }
}
