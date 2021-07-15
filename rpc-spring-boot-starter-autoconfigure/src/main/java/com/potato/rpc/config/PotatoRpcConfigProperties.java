package com.potato.rpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 基础配置
 * @author lizhifu
 */
@ConfigurationProperties(prefix = "potato.rpc")
public class PotatoRpcConfigProperties {
    /**
     * 环境
     */
    private String env = "dev";

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    /**
     * 注册中心
     */
    private String registry;
    /**
     * 服务注册中心地址
     */
    private String registerAddress = "127.0.0.1:2181";
    /**
     * 连接重试次数
     */
    private int retryCount = 3;
    /**
     * 连接超时
     */
    private int connectTimeout = 3000;

    /**
     * 服务暴露端口
     */
    private int port = 9999;
    /**
     * 服务协议
     */
    private String protocol = "java";
    /**
     * 序列化方式
     */
    private String serializerType;
    /**
     * 负载均衡算法
     */
    private String loadBalance = "random";

    public String getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
