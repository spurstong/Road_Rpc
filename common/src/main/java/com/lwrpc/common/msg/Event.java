package com.lwrpc.common.msg;

public class Event {
    //发送正常请求服务消息
    public static final Integer SERVICE = 1;
    //发送心跳消息
    public static final Integer HEART_BEAT = 2;
    //当超过3分钟后没有接收到响应则关闭通道
    public static final Integer HEART_BERT_TIMEOUT = 3;
}
