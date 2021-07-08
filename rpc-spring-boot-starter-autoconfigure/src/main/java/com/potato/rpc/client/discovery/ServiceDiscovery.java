package com.potato.rpc.client.discovery;

import java.util.List;

/**
 * 发现服务
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public interface ServiceDiscovery {
    /**
     * 获取服务信息
     * @param serviceList 服务
     * @return
     */
    void discovery(List<String> serviceList) throws Exception;
}
