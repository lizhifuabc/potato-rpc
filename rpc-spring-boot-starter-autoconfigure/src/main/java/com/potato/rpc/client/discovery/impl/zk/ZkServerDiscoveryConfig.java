package com.potato.rpc.client.discovery.impl.zk;

/**
 * zk配置
 *
 * @author lizhifu
 * @date 2021/6/29
 */
public class ZkServerDiscoveryConfig {
    /**
     * 连接重试次数
     */
    public int retryCount;
    /**
     * 重试间隔：单位毫秒
     */
    public int sleepMsBetweenRetries;
    /**
     * 超时时间，默认3000
     */
    private int sessionTimeout = 3000;
    /**
     * zk地址
     */
    private String zkAddress;
    /**
     * 环境
     */
    private String env;
    /**
     * 端口
     */
    private int port;
    /**
     * 权重
     */
    private int weight;
    /**
     * 服务：默认netty
     */
    private String server;

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getSleepMsBetweenRetries() {
        return sleepMsBetweenRetries;
    }

    public void setSleepMsBetweenRetries(int sleepMsBetweenRetries) {
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "ZkServerDiscoveryConfig{" +
                "retryCount=" + retryCount +
                ", sleepMsBetweenRetries=" + sleepMsBetweenRetries +
                ", sessionTimeout=" + sessionTimeout +
                ", zkAddress='" + zkAddress + '\'' +
                ", env='" + env + '\'' +
                ", port=" + port +
                ", weight=" + weight +
                ", server='" + server + '\'' +
                '}';
    }
}
