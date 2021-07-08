package com.potato.rpc.protocol;

import com.potato.rpc.common.model.RpcMessage;
import com.potato.rpc.common.model.RpcRequest;
import com.potato.rpc.common.model.RpcResponse;
import com.potato.rpc.common.model.ServerInfo;

import java.util.concurrent.CompletableFuture;

/**
 * rpc请求
 *
 * @author lizhifu
 * @date 2021/7/6
 */
public interface PotatoClient {
    /**
     * rpc请求
     * @param rpcMessage
     * @return
     */
    CompletableFuture<RpcResponse> request(RpcMessage rpcMessage, ServerInfo serviceInfo);
}
