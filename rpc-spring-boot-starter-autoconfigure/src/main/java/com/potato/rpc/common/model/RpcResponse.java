package com.potato.rpc.common.model;

import com.potato.rpc.common.constants.PotatoRpcStatusEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求响应
 *
 * @author lizhifu
 * @date 2021/7/1
 */
public class RpcResponse implements Serializable {
    private String requestId;

    private Map<String, String> headers = new HashMap<>();

    private Object returnValue;

    private Exception exception;

    private PotatoRpcStatusEnum potatoRpcStatusEnum;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public PotatoRpcStatusEnum getPotatoRpcStatusEnum() {
        return potatoRpcStatusEnum;
    }

    public void setPotatoRpcStatusEnum(PotatoRpcStatusEnum potatoRpcStatusEnum) {
        this.potatoRpcStatusEnum = potatoRpcStatusEnum;
    }

    @Override
    public String toString() {
        return "PotatoRpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", headers=" + headers +
                ", returnValue=" + returnValue +
                ", exception=" + exception +
                ", potatoRpcStatusEnum=" + potatoRpcStatusEnum +
                '}';
    }
}
