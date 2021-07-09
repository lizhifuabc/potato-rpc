package com.potato.rpc.register;

import com.potato.rpc.base.Destroyable;
import com.potato.rpc.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 注册工厂
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class RegistryFactory {
    private final static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                RegistryFactory.destroy();
            }
        });
    }
    /**
     * 保存全部的配置和注册中心实例
     */
    private final static ConcurrentMap<RegistryConfig, Destroyable> ALL_REGISTRIES = new ConcurrentHashMap<RegistryConfig, Destroyable>();

    public synchronized static void put(RegistryConfig registryConfig,Destroyable serviceRegistry){
        ALL_REGISTRIES.put(registryConfig,serviceRegistry);
    }
    /**
     * 注册中心关闭
     */
    public static void destroy() {
        for (Map.Entry<RegistryConfig, Destroyable> entry : ALL_REGISTRIES.entrySet()) {
            RegistryConfig config = entry.getKey();
            Destroyable registry = entry.getValue();
            try {
                logger.info("service destroy config:{}",config);
                registry.destroy();
                ALL_REGISTRIES.remove(config);
            } catch (Exception e) {

            }
        }
    }
}
