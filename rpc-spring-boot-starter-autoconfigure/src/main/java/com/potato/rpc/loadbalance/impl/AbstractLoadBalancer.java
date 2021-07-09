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
//    /**
//     * 获取权重
//     * @param ServerInfo
//     * @return
//     */
//    protected int getWeight(ServerInfo ServerInfo){
//        if(ServerInfo != null && ServerInfo.isEnable()){
//            return ServerInfo.getWeight();
//        }
//        return -1;
//    }
    @Override
    public ProviderInfo select(List<ProviderInfo> list) {
        if(list == null){
            return null;
        }
        if(list.size() == 1){
            return list.get(0);
        }
        List<ProviderInfo> providerInfoList = new ArrayList<>();
        for (int i = list.size()-1; i >=0 ; i--) {
            ProviderInfo providerInfo = list.get(i);
            if(providerInfo.getEnable() == 1){
                providerInfoList.add(providerInfo);
            }
        }
        return doSelect(providerInfoList);
    }

    /**
     * 获取接口信息
     * @param list 可用接口信息
     * @return 接口信息
     */
    public abstract ProviderInfo doSelect(List<ProviderInfo> list);
}
