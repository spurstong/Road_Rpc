package com.lwrpc.registry.ZK;

import com.lwrpc.registry.data.URL;
import com.lwrpc.registry.data.ZKConsts;
import com.lwrpc.registry.util.FileUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ZKRegister  {
    private static CuratorFramework client = null;
    static {
        init();
    }
    private static void init() {
        RetryPolicy retryPolicy = new RetryNTimes(ZKConsts.RETRYTIME, ZKConsts.SLEEP_MS_BEWTEENR_RETRY);
        client = CuratorFrameworkFactory.builder().connectString(ZKConsts.ZK_SERVER_PATH)
                .sessionTimeoutMs(ZKConsts.SESSION_TIMEOUT_MS).retryPolicy(retryPolicy)
                .namespace(ZKConsts.WORK_SPACE).build();
        client.start();
    }


    public static void register(URL url) {
        try {
            String interfaceName = url.getInterfaceName();
            String implClassName = url.getImplClassName();
            Stat stat = client.checkExists().forPath(getPath(interfaceName, url.toString()));
            if (stat != null) {
                System.out.println("该节点已存在！");
                client.delete().forPath(getPath(interfaceName, url.toString()));
            }
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    //权限控制，任何连接的客户端都可以操作该属性znode
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(getPath(interfaceName, url.toString()), implClassName.getBytes());
            System.out.println(getPath(interfaceName, url.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Map<String, List<URL>> getAllUrl() {
        Map<String, List<URL>> mapList = null;
        try {
            List<String> serviceList = client.getChildren().forPath("/");
            mapList = new HashMap<>(serviceList.size());
            for (String s : serviceList) {
                mapList.put(s, getService(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }


    public static List<URL> getService(String serviceName) {
        List<URL> urls = null;
        try {
            List<String> urlList = client.getChildren().forPath(getPath(serviceName));
            if (urlList != null) {
                if (urlList != null) {
                    urls = new ArrayList<>(urlList.size());
                }
                for (String s : urlList) {
                    String[] url = s.split(":");
                    String imlClassName = get(serviceName, s);
                    urls.add(new URL(url[0], Integer.valueOf(url[1]), serviceName, imlClassName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }

    public static void remove(String url) {
        try {
            List<String> interfaces = client.getChildren().forPath("/");
            for (String anInterface : interfaces) {
                List<String> urlList = client.getChildren().forPath(getPath(anInterface));
                for (String s : urlList) {
                    if (s.equals(url)) {
                        client.delete().forPath(getPath(anInterface, url));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<URL> getServiceInfo(String interfaceName) {
        try {
            addListenerForService(interfaceName);
            System.out.println("开始查找服务节点：" + getPath(interfaceName));
            List<String> urlList = client.getChildren().forPath("/" + interfaceName);
            System.out.println("结果：" + urlList);
            List<URL> result = new ArrayList<>();
            for(String serviceUrl : urlList) {
                String[] urls = serviceUrl.split(":");
                String implClassName = get(interfaceName, serviceUrl);
                System.out.println(implClassName);
                result.add(new URL(urls[0], Integer.valueOf(urls[1]), interfaceName, implClassName));
            }

            return result;
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return null;
    }


    private static void addListenerForService(String serviceName) throws Exception {
        //设置监听，监听所有服务下的节点变化，连接管理收到通知后会移除对应的节点
        final PathChildrenCache childrenCache = new PathChildrenCache(client, getPath(serviceName), true);

        //同步初始监听点
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener(){

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if(event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {
                    //建立完监听
                    return;
                }
                //删除远程服务节点
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    String path = event.getData().getPath();
                    FileUtil fileUtil = new FileUtil(true);
                    fileUtil.alterServiceCache(serviceName, path);
                }
                //节点值更新
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                    String path = event.getData().getPath();
                    System.out.println(String.format("%s,节点值更新", path));
                    byte[] implClass = event.getData().getData();
                }
                //增加了节点
                if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    String path = event.getData().getPath();
                    byte[] implClass = event.getData().getData();
                    System.out.println(String.format("增加了节点,%s", path));
                }
            }
        });
    }
    private static String get(String interfaceName, String url) {
        String res = null;
        try {
            byte[] bytes = client.getData().forPath(getPath(interfaceName, url));
            res = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;

    }

    private static String getPath(String... args){
        StringBuilder builder = new StringBuilder();
        for (String arg:args) {
            builder.append("/").append(arg);
        }
        return builder.toString();
    }

    public static void closeZKClient() {
        if (client != null) {
            client.close();
        }
    }


}
