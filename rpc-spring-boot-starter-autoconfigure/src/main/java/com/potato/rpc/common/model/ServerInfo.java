package com.potato.rpc.common.model;

/**
 * 接口信息
 *
 * @author lizhifu
 * @date 2021/7/1
 */
public class ServerInfo {
    /**
     * IP
     */
    private String ip;
    /**
     * 端口
     */
    private String port;
    /**
     * 权重
     */
    private int weight;
    /**
     * 是否可用
     */
    private boolean isEnable = true;
    /**
     * 服务
     */
    private String server;
    public ServerInfo(){}
    public ServerInfo(String ip, String port, int weight, boolean isEnable, String server) {
        this.server = server;
        this.ip = ip;
        this.port = port;
        this.isEnable = isEnable;
        this.weight = weight;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", weight=" + weight +
                ", isEnable=" + isEnable +
                ", server='" + server + '\'' +
                '}';
    }
}
