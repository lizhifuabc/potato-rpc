package com.potato.rpc.transport.netty.client;

import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.transport.model.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步获取RpcResponse
 *
 * @author lizhifu
 * @date 2021/7/7
 */
public enum CompletableFutureHelper {
    INSTANCE;
    private static Map<String, CompletableFuture<RpcMessage>> map = new ConcurrentHashMap<>();
    public void put(String requestId, CompletableFuture<RpcMessage> future){
        map.put(requestId,future);
    }

    public void remove(String requestId){
        map.remove(requestId);
    }

    public void complete(RpcMessage rpcMessage){
        CompletableFuture<RpcMessage> future = map.get(rpcMessage.getRequestId());
        //事件结束，返回值设置为rpcResponse
        future.complete(rpcMessage);
        map.remove(rpcMessage.getRequestId());
    }

}
