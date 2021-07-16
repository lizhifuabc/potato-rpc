package com.potato.rpc.transport.model;

import java.io.Serializable;

/**
 * rpc消息
 *
 * @author lizhifu
 * @date 2021/7/7
 */
public class RpcMessage implements Serializable {
    /**
     * 请求的Id, 唯一标识该请求
     */
    private String requestId;
    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 请求数据
     */
    private Object data;

    @Override
    public String toString() {
        return "RpcMessage{" +
                "requestId='" + requestId + '\'' +
                ", messageType=" + messageType +
                ", data=" + data +
                '}';
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
