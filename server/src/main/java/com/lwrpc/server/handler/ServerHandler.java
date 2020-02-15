package com.lwrpc.server.handler;

import com.lwrpc.common.msg.Event;
import com.lwrpc.common.msg.LwRequest;
import com.lwrpc.common.msg.LwResponse;
import com.lwrpc.common.netty.NettyUtil;
import com.lwrpc.server.RpcService;
import com.lwrpc.server.beanutil.BeanHolder;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<LwRequest>{
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LwRequest msg) throws Exception {

        //如果收到的是心跳事件
        if (msg.getEventType() == Event.HEART_BEAT) {
            NettyUtil.updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
            LwResponse heartBeatRes = new LwResponse();
            heartBeatRes.setEventType(Event.HEART_BEAT);
            System.out.println("服务端收到了心跳消息");
            channelHandlerContext.writeAndFlush(heartBeatRes).addListeners((ChannelFutureListener)future->{
                if(!future.isSuccess()) {
                    log.error("IO error, close Channel");
                    future.channel().close();
                }
            });
        } else {
            LwResponse lwResponse = new LwResponse();
            lwResponse.setRequestId(msg.getRequestId());
            lwResponse.setEventType(Event.SERVICE);
            log.info("从客户端接收到请求信息:{}", msg);
            try {
                Object result = handler(msg);
                System.out.println("服务端处理了消息");
                lwResponse.setResult(result);
            } catch (Throwable throwable) {
                lwResponse.setCause(throwable);
                throwable.printStackTrace();

            }
            channelHandlerContext.writeAndFlush(lwResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            System.out.println("服务端未接收心跳消息，关闭");
            ctx.channel().close();
        }

        super.userEventTriggered(ctx, evt);
    }

    private Object handler(LwRequest request) throws Exception {
        System.out.println("服务端获取的class:" + request.getClassName());

//        Class<?> clazz = Class.forName(request.getClassName());
//        Class<?> class1 = Class.forName(request.getImplClassName());
//        System.out.println(beanHolder.getBeansWithAnnotation(RpcService.class));
//        Object serviceBean = beanHolder.getBean(class1);
//        System.out.println(serviceBean);
//        String methodName = request.getMethodName();
//        Class<?>[] parameterTypes = request.getParameterTypes();
//        Object[] paramethers = request.getParameters();
//        Class<?> serviceClass = serviceBean.getClass();
//        FastClass fastClass = FastClass.create(serviceClass);
//        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
//        Object result = fastMethod.invoke(fastClass.newInstance(), paramethers);

        Class<?> serviceClass = Class.forName(request.getImplClassName());
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] paramethers = request.getParameters();
        Method method = serviceClass.getMethod(methodName,parameterTypes);
        Object result = method.invoke(serviceClass.newInstance(), paramethers);

        return result;
    }
}
