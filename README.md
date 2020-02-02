# lightWeightRpc
**轻量级RPC第四版**

不同版本存放在了不同branch上

# 新增内容
- 支持服务发现机制（SPI）
- 对Spring的SPI机制进行改进，解决依赖注入问题

本版本可以通过通过配置文件和自定义的`RoadSPI`注解修改序列化方式

配置文件位置 `META-INF/roadspi/`

文件名以序列化接口全路径命名
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
