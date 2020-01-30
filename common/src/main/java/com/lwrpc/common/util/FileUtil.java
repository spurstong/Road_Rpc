package com.lwrpc.common.util;

import com.lwrpc.registry.data.URL;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class FileUtil  {
    private final String filrDir = "cache";
    private final String fileName = "serviceAddress.txt";
    private final Properties properties = new Properties();
    private final AtomicLong lastCacheChanged = new AtomicLong();
    private final AtomicInteger savePropertiesRetryTimes = new AtomicInteger();
    private final int SAVE_MAX_RETRY = 3;
    private boolean syncSaveFile;
    private File file;
    private final ExecutorService registerCacheExecutor = Executors.newFixedThreadPool(1);


    public FileUtil(boolean isSync) {
        syncSaveFile = isSync;
        File file = new File(filrDir + "/" + fileName);
        if (!file.exists()) {
            file.getParentFile().mkdir();
        }
        this.file = file;
        loadProperties();

    }

    private void loadProperties() {
        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                System.out.println("------------------------");
                in = new FileInputStream(file);
                properties.load(in);
            } catch (Throwable e) {
                log.warn("加载缓存服务信息失败！");
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public List<URL> getServiceUrls(String serviceName) {
        List<URL> urls = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            System.out.println(value);
            if (key != null && key.equals(serviceName)) {
                String[] strs = value.split(";");
                for (String str: strs) {
                    String[] info = str.split("_");
                    String[] address = info[0].split(":");
                    urls.add(new URL(address[0], Integer.valueOf(address[1]), info[1], info[2]));
                }
                return urls;
            }
        }
        return null;
    }

    private void doSaveProperties(long version) {
        if (version < lastCacheChanged.get())
            return;
        if (file == null)
            return;
        try {
            File lockfile = new File(file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                lockfile.createNewFile();
            }
            try(RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
            FileChannel channel = raf.getChannel();) {
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    throw new IOException("不能锁住注册的缓存文件");
                }
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try (FileOutputStream outputFile = new FileOutputStream(file)) {
                        properties.store(outputFile, "RPC Server Cache");
                    }
                } finally {
                    lock.release();
                }
            }
        }catch (Throwable e) {
            savePropertiesRetryTimes.incrementAndGet();
            if (savePropertiesRetryTimes.get() > SAVE_MAX_RETRY) {
                log.warn("超过最大重试次数，缓存失败！");
                savePropertiesRetryTimes.set(0);
                return;
            }
            if (version < lastCacheChanged.get()) {
                savePropertiesRetryTimes.set(0);
                return;
            }
            e.printStackTrace();
        }

    }

    public void saveServices(String serviceName, List<URL> urlList) {
        if (file == null)
            return;
        try {
            StringBuilder buf = new StringBuilder();
            for(URL url : urlList) {
                if (buf.length() > 0) {
                    buf.append(";");
                }
                buf.append(url.getAllInformation());
            }

            properties.setProperty(serviceName, buf.toString());
            long version = lastCacheChanged.incrementAndGet();
            if (syncSaveFile) {
                doSaveProperties(version);
            } else {
                registerCacheExecutor.execute(new SaveProperties(version));
            }

        } catch (Throwable  t) {
            log.warn(t.getMessage(), t);
        }
    }


    private class SaveProperties implements Runnable {
        private long version;
        private SaveProperties(long version) {
            this.version = version;
        }

        @Override
        public void run() {
            doSaveProperties(version);
        }
    }





}
