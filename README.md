# lightWeightRpc
轻量级RPC第一版

利用springboot实现依赖注入与参数配置

利用Netty来构建服务端和客户端

实现Hessian来实现序列化

# RPC启动
先启动服务端，再启动客户端

**服务端**
```
@SpringBootApplication
public class ServerApplication implements ApplicationRunner {
    @Autowired
    NettyServer nettyServer;
    public static void main(String[] args)  {

        SpringApplication.run(ServerApplication.class, args);
    }
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        nettyServer.start();
    }

}
```
**客户端**
```
@SpringBootApplication
public class CommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonApplication.class, args);
    }

}
```
