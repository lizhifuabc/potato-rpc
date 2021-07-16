package com.potato.rpc.register;

import com.potato.rpc.config.RegistryConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务发现
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public abstract class AbstractDiscovery implements ServiceDiscovery {
    /**
     * key: serviceName
     */
    protected Map<String, List<ProviderInfo>> SERVER_MAP = new ConcurrentHashMap<>();
    /**
     * 注册中心配置
     */
    protected RegistryConfig registryConfig;
    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    protected AbstractDiscovery(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    @Override
    public List<ProviderInfo> getProviderInfo(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }

}
