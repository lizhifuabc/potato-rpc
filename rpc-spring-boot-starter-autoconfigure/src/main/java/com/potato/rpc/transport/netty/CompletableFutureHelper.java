package com.potato.rpc.transport.netty;

import com.potato.rpc.common.model.RpcResponse;

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
    private static Map<String, CompletableFuture<RpcResponse>> map = new ConcurrentHashMap<>();
    public void put(String requestId, CompletableFuture<RpcResponse> future){
        map.put(requestId,future);
    }

    public void remove(String requestId){
        map.remove(requestId);
    }

    public void complete(RpcResponse rpcResponse){
        CompletableFuture<RpcResponse> future = map.get(rpcResponse.getRequestId());
        //事件结束，返回值设置为rpcResponse
        future.complete(rpcResponse);
        map.remove(rpcResponse.getRequestId());
    }

}
