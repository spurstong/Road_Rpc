# lightWeightRpc
`**轻量级RPC第三版**`
不同版本存放在了不同branch上

# 新增内容
- 利用Netty的`Promise`来实现传送的异步
- 构建线程池`LwRequestPool`来管理发送的请求线程
- 添加服务缓存机制，确保在注册中心宕机后仍能进行服务调用,在缓存机制利用version号等信息，确保在高并发情况下的信息准确
# 快速开始

启动顺序
1. 安装zookeeper,进入bin目录，运行命令`./zkServer.sh start`
2. 运行zookeeper服务监控
3. 运行服务端
4. 运行客户端

**Zookeeper服务监控**
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
**服务端**
```java
@SpringBootApplication
public class ServerApplication  {

    public static void main(String[] args)  {

        SpringApplication.run(ServerApplication.class, args);
    }

}
```
**客户端**
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
