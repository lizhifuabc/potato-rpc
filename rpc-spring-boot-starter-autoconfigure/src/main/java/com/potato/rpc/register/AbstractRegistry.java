package com.potato.rpc.register;

import com.potato.rpc.config.RegistryConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 注册中心
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public abstract class AbstractRegistry implements ServiceRegistry{
    /**
     * 服务发布信息
     */
    protected ConcurrentMap<String, ProviderInfo> providerInfoMap = new ConcurrentHashMap<String, ProviderInfo>();
    @Override
    public ProviderInfo getProviderInfo(String serviceName) {
        return providerInfoMap.get(serviceName);
    }
    /**
     * 注册中心配置
     */
    protected RegistryConfig registryConfig;
    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    protected AbstractRegistry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

}
