package com.potato.rpc.config;

import java.io.Serializable;

/**
 * 注册中心配置
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class RegistryConfig implements Serializable {
    /**
     * 连接注册中心超时时间
     */
    private int connectTimeout;
    /**
     * 注册中心的地址
     */
    private String address;
    /**
     * 环境
     */
    private String env;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
