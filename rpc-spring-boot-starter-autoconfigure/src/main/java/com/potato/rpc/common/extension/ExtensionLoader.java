package com.potato.rpc.common.extension;

import com.potato.rpc.common.exception.PotatoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 扩展loader spi方式：dubbo
 *
 * @author lizhifu
 * @date 2021/7/13
 */
public class ExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);
    /**
     * spi配置文件路径
     */
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    /**
     * 存放ExtensionLoader,为所有的ExtensionLoader类的实例对象所共享
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(16);
    /**
     * 存放所有的实例化类,为所有的ExtensionLoader类的实例对象所共享
     */
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    /**
     * spi接口实现类的实例
     * 配置文件中的key和value
     */
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    /**
     * 接口类
     */
    private final Class<?> type;
    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    /**
     * 创建目标spi的ExtensionLoader
     * @param type 注解spi的类
     * @param <T> 注解spi的类
     * @return loader ExtensionLoader实例
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new PotatoRuntimeException("Extension type is null");
        }
        if (!type.isInterface()) {
            throw new PotatoRuntimeException("Extension type (" + type + ") is not an interface!");
        }
        if (!type.isAnnotationPresent(SPI.class)) {
            throw new PotatoRuntimeException("Extension type (" + type +
                    ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            //创建spi的ExtensionLoader
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    /**
     * 根据name查询实现类
     * @param name 配置文件中的key
     * @return 实例化类
     */
    public T getExtension(String name) {
        if (name == null || name.isEmpty()) {
            throw new PotatoRuntimeException("Extension name should not be null or empty.");
        }
        //获取一个实体类，持有一个 volatile 的 interface 实现类对象，保证内存可见性
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            //如果缓存中没有的话在创建一个然后放进去，但是此时并没有实际内容，只有一个空的容器Holder
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            //双重检测
            synchronized (holder) {
                //重新获取instance，通过volatile，保障可见性
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双重检测
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    //加载配置文件所有数据
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 加载配置文件数据
     * @param extensionClasses
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        //文件名称：META-INF/extensions/className
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            Enumeration<URL> urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Extension loadDirectory IOException",e);
        }
    }
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                throw new RuntimeException("createExtension exception",e);
            }
        }
        return instance;
    }

    /**
     * 配置文件加载
     * @param extensionClasses 解析数据保存
     * @param classLoader classLoader
     * @param resourceUrl 文件地址
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader,URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            //读取配置文件数据
            while ((line = reader.readLine()) != null) {
                //去掉配置文件注释
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                //去除空格
                line = line.trim();
                if (line.length() > 0) {
                    int i = line.indexOf('=');
                    //只解析key=value形式的数据
                    if (i > 0) {
                        String name = line.substring(0, i).trim();
                        String clazzName = line.substring(i + 1).trim();
                        //加载class
                        Class<?> clazz = classLoader.loadClass(clazzName);
                        extensionClasses.put(name, clazz);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new PotatoRuntimeException("Extension loadResource Exception",e);
        }
    }
}
