package com.potato.rpc.common.model;

import java.io.Serializable;

/**
 * rpc消息
 *
 * @author lizhifu
 * @date 2021/7/7
 */
public class RpcMessage implements Serializable {
    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 请求数据
     */
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "RpcMessage{" +
                "messageType=" + messageType +
                ", data=" + data +
                '}';
    }
}
