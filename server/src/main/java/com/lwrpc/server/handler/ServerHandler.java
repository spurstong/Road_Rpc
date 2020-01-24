package com.lwrpc.server.handler;

import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<LwRequest> implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LwRequest msg) throws Exception {
        LwResponse lwResponse = new LwResponse();
        lwResponse.setRequestId(msg.getRequestId());
        log.info("从客户端接收到请求信息:{}", msg);
        try {
            Object result = handler(msg);
            lwResponse.setResult(result);
        } catch (Throwable throwable) {
            lwResponse.setCause(throwable);
            throwable.printStackTrace();

        }
        channelHandlerContext.writeAndFlush(lwResponse);
    }

    private Object handler(LwRequest request) throws ClassNotFoundException, InvocationTargetException {

        Class<?> clazz = Class.forName(request.getClassName());
        Object serviceBean = applicationContext.getBean(clazz);
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        log.info("获取到的服务类:{}", serviceBean);
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] paramethers = request.getParameters();
        // 使用CGLIB 反射
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        return fastMethod.invoke(serviceBean, paramethers);
    }
}
