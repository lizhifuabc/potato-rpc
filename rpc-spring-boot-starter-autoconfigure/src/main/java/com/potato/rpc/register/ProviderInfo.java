package com.potato.rpc.register;

import com.alibaba.fastjson.JSONObject;

/**
 * 服务提供信息
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class ProviderInfo {
    /**
     * 具体服务
     */
    private Object obj;
    /**
     * 接口名称
     */
    private String serviceName;
    /**
     * 序列化方式
     */
    private String serializerType;
    /**
     * IP
     */
    private String ip;
    /**
     * 端口
     */
    private int port;
    /**
     * 权重
     */
    private int weight;
    /**
     * 是否可用1:可用，0：不可用
     */
    private int enable;

    public String getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
    }

    /**
     * json数据
     * @return json格式数据
     */
    public String json(){
        JSONObject jsonChildData = new JSONObject();
        jsonChildData.put("weight", weight);
        jsonChildData.put("enable", enable);
        jsonChildData.put("serializerType", serializerType);
        return jsonChildData.toJSONString();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
