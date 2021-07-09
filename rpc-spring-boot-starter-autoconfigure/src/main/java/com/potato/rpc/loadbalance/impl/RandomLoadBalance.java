package com.potato.rpc.loadbalance.impl;

import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.register.ProviderInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机算法
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public class RandomLoadBalance extends AbstractLoadBalancer {
    private static Random random = new Random();
    @Override
    public ProviderInfo doSelect(List<ProviderInfo> list) {
        return list.get(random.nextInt(list.size()));
    }

    public static void main(String[] args) {
        List<ProviderInfo> services = new ArrayList<>(3);
        ProviderInfo service = new ProviderInfo();
        service.setIp("196.128.6.1");
        service.setWeight(1);
        services.add(service);

        ProviderInfo service2 = new ProviderInfo();
        service2.setIp("196.128.6.2");
        service2.setWeight(3);
        services.add(service2);

        ProviderInfo service3 = new ProviderInfo();
        service3.setIp("196.128.6.3");
        service3.setWeight(5);
        services.add(service3);

        LoadBalancer loadBalance = new RandomLoadBalance();
        System.out.println("20次请求负载均衡结果为:");
        for(int i=1;i<=20;i++){
            System.out.println("第"+i+"次请求服务ip为："+loadBalance.select(services).getIp());
        }
    }
}
