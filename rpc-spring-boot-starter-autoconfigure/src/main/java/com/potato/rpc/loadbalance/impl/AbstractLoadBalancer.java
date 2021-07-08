package com.potato.rpc.loadbalance.impl;

import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.common.model.ServerInfo;

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
     * 获取权重
     * @param ServerInfo
     * @return
     */
    protected int getWeight(ServerInfo ServerInfo){
        if(ServerInfo != null && ServerInfo.isEnable()){
            return ServerInfo.getWeight();
        }
        return -1;
    }
    @Override
    public ServerInfo select(List<ServerInfo> list) {
        if(list == null){
            return null;
        }
        if(list.size() == 1){
            return list.get(0);
        }
        List<ServerInfo> serverInfoList = new ArrayList<>();
        for (int i = list.size()-1; i >=0 ; i--) {
            ServerInfo ServerInfo = list.get(i);
            if(ServerInfo.isEnable()){
                serverInfoList.add(ServerInfo);
            }
        }
        return doSelect(serverInfoList);
    }

    /**
     * 获取接口信息
     * @param list 可用接口信息
     * @return 接口信息
     */
    public abstract ServerInfo doSelect(List<ServerInfo> list);
}
