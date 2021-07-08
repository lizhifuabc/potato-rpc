package com.potato.rpc.server.cache;

import com.potato.rpc.common.model.ServiceObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册本地缓存
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class ServerRegistryCache {
    /**
     * key: serviceName
     */
    public static final Map<String, ServiceObject> SERVER_MAP = new ConcurrentHashMap<>();
}
