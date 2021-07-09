package com.potato.rpc.register;

import com.potato.rpc.base.Destroyable;
import com.potato.rpc.base.Initializable;
import com.potato.rpc.base.Startable;

import java.util.List;

/**
 * 服务发现
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public interface ServiceDiscovery extends Destroyable, Initializable, Startable {
    /**
     * 服务发现
     *
     * @param serviceName 服务名称
     */
    public void discovery(String serviceName);

    /**
     * 获取服务接口信息
     * @param serviceName
     * @return
     */
    public List<ProviderInfo> getProviderInfo (String serviceName);
}
