package com.potato.rpc.common.model;

/**
 * ServiceObject
 *
 * @author lizhifu
 * @date 2021/7/1
 */
public class ServiceObject {
    /**
     * 具体服务
     */
    private Object obj;
    /**
     * 接口名称
     */
    private String serviceName;
    /**
     * 接口信息
     */
    private ServerInfo serverInfo;

    @Override
    public String toString() {
        return "ServiceObject{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceInfo=" + serverInfo +
                '}';
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServerInfo getServiceInfo() {
        return serverInfo;
    }

    public void setServiceInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}
