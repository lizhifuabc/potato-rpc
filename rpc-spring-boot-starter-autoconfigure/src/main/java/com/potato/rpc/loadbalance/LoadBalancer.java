package com.potato.rpc.loadbalance;

import com.potato.rpc.common.model.ServerInfo;
import com.potato.rpc.register.ProviderInfo;

import java.util.List;

/**
 * 负载算法
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public interface LoadBalancer {
    /**
     * 获取接口信息
     * @param list
     * @return 接口信息
     */
    ProviderInfo select(List<ProviderInfo> list);
}
