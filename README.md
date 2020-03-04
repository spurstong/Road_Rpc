# Road_Rpc
**轻量级RPC第五版（最终版）**

不同版本在不同分支上

主要功能和特点：
- 利用Spring实现依赖注入与参数配置
- 利用Netty来实现客户端与服务端的远程通信
- 利用Hessian来实现序列化
- 设置Zookeeper作为注册中心
- 新设监控器，通过心跳机制来判断服务端与监控器的网络连接状况，当出现不稳定时，认为服务端出现了问题，在注册中心删除相关的服务信息。
- 利用Netty的Promise来实现异步的传送
- 构建线程池来管理发送的请求线程
- 添加服务缓存机制，在注册中心宕机的情况下仍能进行服务消费。
- 支持服务扩展点发现机制（SPI）,对Spring的SPI机制进行改进，解决了依赖注入问题。
- 在客户端从注册中心获取服务时，添加监听器，当注册中心对应节点发生变化时通知客户端修改本地缓存信息。
# 服务暴露
```java
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello, " +name;
    }
}
```
# SPI机制切换序列化方式
```java
//序列化接口
@RoadSpi("hession")
public interface Serializer {
    //将java对象转换为二进制
    byte[] serialize(Object object) throws IOException;
    //将二进制文件转化为java对象
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
```
# 监控中心启动
```java
@SpringBootApplication
public class RegistryApplication implements ApplicationRunner {
    @Autowired
    private ZKServer zkServer;
    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        zkServer.start();
   }
}
```
# 服务端启动
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.lwrpc.*"})
public class ServerApplication  {
    public static void main(String[] args)  {
        SpringApplication.run(ServerApplication.class, args);
    }
}
```
# 客户端启动
```java
@SpringBootApplication
@Slf4j
public class ClientApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ClientApplication.class, args);
        HelloService helloService = ProxyFactory.create(HelloService.class);
        String result = helloService.hello("阿走");
        log.info("请求服务结果:{}", result);
    }
}
```
# 快速启动
1. 先打开监控中心
2. 然后开启服务端
3. 最后开启客户端
