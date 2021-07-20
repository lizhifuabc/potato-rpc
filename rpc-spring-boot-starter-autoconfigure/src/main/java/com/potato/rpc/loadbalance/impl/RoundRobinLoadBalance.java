package com.potato.rpc.loadbalance.impl;

import com.potato.rpc.register.ProviderInfo;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 加权轮询算法:dubbo
 *
 * @author lizhifu
 * @date 2021/7/19
 */
public class RoundRobinLoadBalance extends AbstractLoadBalancer{
    /**
     * key:com.potato.api.UserService、com.potato.api.UserService2
     * value:{key:127.0.0.1:8080,value:WeightedRoundRobin}
     */
    private final ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<String, ConcurrentMap<String, WeightedRoundRobin>>();
    private static final int RECYCLE_PERIOD = 60000;
    @Override
    public ProviderInfo doSelect(List<ProviderInfo> list) {
        // 接口名称
        // com.potato.api.UserService
        // com.potato.api.UserService2
        String key = list.get(0).getServiceName();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        // 权重总和
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        //当前时间
        long now = System.currentTimeMillis();
        WeightedRoundRobin selectedWRR = null;
        ProviderInfo selectProviderInfo = null;
        // 权重计算：本次权重 = 前次权重(0) + 默认权重
        // 如果选中增加计算：本次权重 = 本次权重 - 总权重
        // A、B、C三者的权重分别是5 3 1，总权重为 9
        // 第一次：cur【5，3，1】  计算权重：【5，3，1】 选择A,A=5-9=-4，最终结果：【-4，3，1】
        // 第二次：cur【-4，3，1】 计算权重：【1，6，2】 选择B,B=6-9=-3, 最终结果：【1，-3，2】
        // 综上：计算结果的总权重会保持不变，最终结果的和是 0
        for (ProviderInfo providerInfo : list) {
            // key：127.0.0.1:8080
            String ipAndPort = providerInfo.getIp()+":"+providerInfo.getPort();
            int weight = getWeight(providerInfo);
            //初始化weight
            WeightedRoundRobin weightedRoundRobin = map.computeIfAbsent(ipAndPort, k -> {
                WeightedRoundRobin wrr = new WeightedRoundRobin();
                wrr.setWeight(weight);
                return wrr;
            });
            // 权重发生了改变
            if (weight != weightedRoundRobin.getWeight()) {
                weightedRoundRobin.setWeight(weight);
            }
            // 设置最新时间
            weightedRoundRobin.setLastUpdate(now);
            // 原子自增操作 cur = cur + weight
            long cur = weightedRoundRobin.increaseCurrent();
            // 选中权重最大的
            if (cur > maxCurrent) {
                // 将本次获取的值作为最大值进行选中
                // 这样等到下次获取时，直接判断是否和本次进行对比
                maxCurrent = cur;
                selectProviderInfo = providerInfo;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if (list.size() != map.size()) {
            map.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
        }
        if (selectProviderInfo != null) {
            // 将本次选中的轮训值设置为 (-1 * total)+current 即选中的权重=当全权重-总权重
            selectedWRR.sel(totalWeight);
            return selectProviderInfo;
        }
        return list.get(0);
    }
    protected static class WeightedRoundRobin {
        // 服务提供者权重
        private int weight;
        // 当前权重
        private AtomicLong current = new AtomicLong(0);
        // 最后一次更新时间
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            // 初始情况下，current = 0
            current.set(0);
        }

        public long increaseCurrent() {
            // current = current + weight
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
}
