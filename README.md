# lightWeightRpc
轻量级RPC第二版

# 新增内容：
- 设置zookeeper作为注册中心
- 新设监控管理服务器，通过心跳机制来判断服务端与监控器之间的网络连接情况，当服务端与监控器间的连接不稳定时认定服务端出现问题，在注册中心中删除属于该问题服务端中的服务信息。

# 快速启动

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

