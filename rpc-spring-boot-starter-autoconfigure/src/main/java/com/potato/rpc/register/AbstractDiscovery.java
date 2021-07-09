package com.potato.rpc.register;

import com.potato.rpc.config.RegistryConfig;
import com.potato.rpc.register.ServiceDiscovery;

/**
 * 服务发现
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public abstract class AbstractDiscovery implements ServiceDiscovery {
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

}
