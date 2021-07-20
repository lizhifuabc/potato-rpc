package com.potato.rpc.loadbalance.impl;

import com.potato.rpc.register.ProviderInfo;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机算法
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public class RandomLoadBalance extends AbstractLoadBalancer {
    @Override
    public ProviderInfo doSelect(List<ProviderInfo> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
