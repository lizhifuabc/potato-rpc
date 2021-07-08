package com.potato.rpc.register;

import com.potato.rpc.config.RegistryConfig;

/**
 * 注册中心
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public abstract class AbstractRegistry implements ServiceRegistry{
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
