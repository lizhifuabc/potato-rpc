package com.potato.rpc.transport.model;

/**
 * 返回消息类型
 *
 * @author lizhifu
 * @date 2021/7/7
 */
public class ResponseMessageType {
    /**
     * 普通请求
     */
    public static final byte RESPONSE_TYPE_NORMAL = 3;
    /**
     * 心跳请求
     */
    public static final byte RESPONSE_TYPE_HEARTBEAT = 4;
}
