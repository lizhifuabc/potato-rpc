package com.potato.rpc.client.cache;

import com.potato.rpc.common.model.ServerInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务发现本地缓存
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class ServerDiscoveryCache {
    /**
     * key: serviceName
     */
    public static final Map<String, List<ServerInfo>> SERVER_MAP = new ConcurrentHashMap<>();
}
