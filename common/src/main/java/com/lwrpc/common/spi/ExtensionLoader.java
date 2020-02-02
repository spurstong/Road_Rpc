package com.lwrpc.common.spi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ExtensionLoader implements ApplicationContextAware {
    private static final ConcurrentHashMap<Class<?>, Map<String, Class<?>>> cacheClasses= new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Map<String, Object>> cacheIntances = new ConcurrentHashMap<>();

    private static final String SERVICE_DIR = "META-INF/roadspi/";


    private ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public Object getServiceExtension(Class<?> type, String... args) {
        if (type == null) {
            log.error("Extension type is null");
        }
        if (!type.isInterface()) {
            log.error("Extention type is not interface!");
        }
        String serviceName = null;
        if (args == null || args.length == 0) {
            final RoadSpi roadSpi = type.getAnnotation(RoadSpi.class);
            String defaultName = roadSpi.value().trim();
            if (defaultName != null && defaultName.length() > 0) {
                serviceName = defaultName;
            }
            log.error("no extention name!");
        } else {
            serviceName = args[0];
        }
        System.out.println("setice name :" + serviceName);
        Object result = null;
        Map<String, Object> extensionInstanceMap = cacheIntances.get(type);
        if (extensionInstanceMap != null) {
            result = extensionInstanceMap.get(serviceName);
            if (result != null) {
                return result;
            } else {
                System.out.println("************************");
                if (cacheClasses.get(serviceName) == null) {
                    Map<String, Class<?>> extensionClasses = new HashMap<>();
                    loadDirectory(extensionClasses, type);
                    cacheClasses.put(type, extensionClasses);
                }
                Map<String, Class<?>> serviceClass = cacheClasses.get(type);
                if (serviceClass == null) {
                    log.error("cache class is null!");
                }
                createService(extensionInstanceMap,serviceClass, serviceName, type);
            }
        }
        if (extensionInstanceMap == null) {
            synchronized (ExtensionLoader.class) {
                extensionInstanceMap = cacheIntances.get(type);
                if (extensionInstanceMap == null) {
                    System.out.println("ccccccc");
                    extensionInstanceMap = new HashMap<String, Object>();
                    Map<String, Class<?>> extensionClasses = new HashMap<>();
                    loadDirectory(extensionClasses, type);
                    System.out.println(extensionClasses.size());
                    cacheClasses.put(type, extensionClasses);
                    Map<String, Class<?>> serviceClass = cacheClasses.get(type);
                    if (serviceClass == null) {
                        log.error("cache class is null!");
                    }
                    createService(extensionInstanceMap, serviceClass, serviceName, type);
                }
            }
        }
        extensionInstanceMap = cacheIntances.get(type);
        result = extensionInstanceMap.get(serviceName);
        return result;
    }

    private void createService(Map<String, Object> extensionInstanceMap, Map<String, Class<?>> serviceClass, String serviceName, Class<?> type) {
        Class<?> obj = serviceClass.get(serviceName);
        if (obj == null) {
            log.error("serviceClass is null!");
        }
        String beanName = obj.getSimpleName().concat(serviceName);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(obj);
        GenericBeanDefinition definition = (GenericBeanDefinition)builder.getRawBeanDefinition();
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext)context;
        DefaultListableBeanFactory register = (DefaultListableBeanFactory)configurableApplicationContext.getBeanFactory();
        register.registerBeanDefinition(beanName, definition);
        extensionInstanceMap.put(serviceName, context.getBean(beanName));
        cacheIntances.put(type, extensionInstanceMap);
    }
    private void loadDirectory(Map<String, Class<?>> extensionClasses, Class<?> type) {
        String path = SERVICE_DIR + type.getCanonicalName();
        System.out.println(path);
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = DecoratingClassLoader.class.getClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(path);
            } else {
                urls = ClassLoader.getSystemResources(path);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourcesURL = urls.nextElement();
                    loadResources(extensionClasses, classLoader, resourcesURL);
                }
            }
        } catch (Throwable t) {
            log.error("load interface file failed, path:" + path);
        }
    }

    private void loadResources(Map<String, Class<?>> extentionClasses, ClassLoader classLoader, URL resourcesURL) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourcesURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        String name = null;
                        int i = line.indexOf('=');
                        if (i > 0) {
                            name = line.substring(0, i).trim();
                            line = line.substring(i+1).trim();
                            System.out.println("name=:" + name + "line:" + line);
                        }
                        if (line.length() > 0) {
                            extentionClasses.put(name, Class.forName(line, true, classLoader));
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
