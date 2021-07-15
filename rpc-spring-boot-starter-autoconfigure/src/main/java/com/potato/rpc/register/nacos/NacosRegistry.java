package com.potato.rpc.register.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.config.RegistryConfig;
import com.potato.rpc.register.AbstractRegistry;
import com.potato.rpc.register.ProviderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Nacos注册中心
 *
 * @author lizhifu
 * @date 2021/7/15
 */
public class NacosRegistry extends AbstractRegistry {
    /**
     * 服务发布信息
     */
    private ConcurrentMap<String, ProviderInfo> providerInfoMap = new ConcurrentHashMap<String, ProviderInfo>();
    private final static Logger logger = LoggerFactory.getLogger(NacosRegistry.class);
    private NamingService namingService;
    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    public NacosRegistry(RegistryConfig registryConfig) {
        super(registryConfig);
    }
    @Override
    public void destroy() {
        if (namingService != null && namingService.getServerStatus() == "UP") {
            try {
                namingService.shutDown();
            } catch (NacosException e) {
                logger.error("NacosRegistry destroy NacosException",e);
            }
        }
        providerInfoMap.clear();
    }

    @Override
    public void init() {
        //保障只初始化一次
        if (namingService != null) {
            return;
        }
        try {
            namingService = NamingFactory.createNamingService(registryConfig.getAddress());
        } catch (NacosException e) {
            throw new PotatoRuntimeException("NacosRegistry init NacosException",e);
        }
    }

    @Override
    public synchronized boolean start() {
        if (namingService == null) {
            throw new PotatoRuntimeException("NacosRegistry not init");
        }
        if (namingService.getServerStatus() == "UP") {
            logger.info("NacosRegistry start success:{}",registryConfig);
            return true;
        }
        if (namingService.getServerStatus() == "DOWN") {
            logger.info("NacosRegistry start fail:{}",registryConfig);
            return false;
        }
        return false;
    }

    @Override
    public void register(ProviderInfo providerInfo) {
        providerInfoMap.putIfAbsent(providerInfo.getServiceName(),providerInfo);

        Instance instance = new Instance();
        instance.setServiceName(providerInfo.getServiceName());
        instance.setIp(providerInfo.getIp());
        instance.setPort(providerInfo.getPort());
        instance.setHealthy(true);
        instance.setWeight(providerInfo.getWeight());

        Map<String, String> instanceMeta = new HashMap<>();
        instanceMeta.put("providerInfo", providerInfo.json());
        instance.setMetadata(instanceMeta);
        try {
            namingService.registerInstance(providerInfo.getServiceName(),registryConfig.getEnv(), instance);
        } catch (NacosException e) {
            throw new PotatoRuntimeException("NacosRegistry registry exception", e);
        }
    }

    @Override
    public ProviderInfo getProviderInfo(String serviceName) {
        return providerInfoMap.get(serviceName);
    }
}
