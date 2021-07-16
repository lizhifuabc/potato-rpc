package com.potato.rpc.loadbalance;

import com.potato.rpc.common.extension.SPI;
import com.potato.rpc.register.ProviderInfo;

import java.util.List;

/**
 * 负载算法
 *
 * @author lizhifu
 * @date 2021/6/25
 */
@SPI
public interface LoadBalancer {
    /**
     * 获取接口信息
     * @param list
     * @return 接口信息
     */
    ProviderInfo select(List<ProviderInfo> list);
}
