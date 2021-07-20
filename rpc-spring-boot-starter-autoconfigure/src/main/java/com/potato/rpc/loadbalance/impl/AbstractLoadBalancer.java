package com.potato.rpc.loadbalance.impl;

import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.register.ProviderInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象方法
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {
    /**
     * 预热权重
     * @param uptime 服务运行时间
     * @param warmup 预热时间
     * @param weight 权重
     * @return 随着服务运行时间 uptime 增大，权重计算值 ww 会慢慢接近配置值 weight
     */
    public static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ( uptime / ((float) warmup / weight));
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }
    /**
     * 获取权重
     * @param  providerInfo
     * @return 权重
     */
    protected int getWeight(ProviderInfo providerInfo){
        // 服务启动时长
        long uptime = System.currentTimeMillis() - providerInfo.getUpTime();
        // 防止服务器时间不同步的情况发生
        if (uptime < 0) {
            return 1;
        }
        // 10 分钟预热时间
        int warmup = 10 * 60 * 1000;
        // 已经预热完成
        if(uptime > warmup){
            return providerInfo.getWeight();
        }
        return calculateWarmupWeight((int)uptime,warmup,providerInfo.getWeight());
    }
    @Override
    public ProviderInfo select(List<ProviderInfo> list) {
        if(list == null || list.size() == 0){
            return null;
        }
        //只有一个提供者，直接返回即可，无需进行负载均衡
        if(list.size() == 1){
            return list.get(0).getEnable() == 1 ? list.get(0) : null;
        }
        List<ProviderInfo> providerInfoList = new ArrayList<>();
        //剔除不可用provider
        for (int i = list.size()-1; i >=0 ; i--) {
            ProviderInfo providerInfo = list.get(i);
            if(providerInfo.getEnable() == 1){
                providerInfoList.add(providerInfo);
            }
        }
        // 调用 doSelect 方法进行负载均衡，该方法为抽象方法，由子类实现
        return doSelect(providerInfoList);
    }

    /**
     * 获取接口信息
     * @param list 可用接口信息
     * @return 接口信息
     */
    public abstract ProviderInfo doSelect(List<ProviderInfo> list);
}
