package com.potato.rpc.register;

import com.potato.rpc.base.Destroyable;
import com.potato.rpc.base.Initializable;
import com.potato.rpc.base.Startable;

/**
 * 服务注册中心
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public interface ServiceRegistry extends Destroyable, Initializable, Startable {
    /**
     * 服务注册
     *
     * @param providerInfo 服务注册信息
     */
    public void register(ProviderInfo providerInfo);

    /**
     * 获取服务接口信息
     * @param serviceName
     * @return
     */
    public ProviderInfo getProviderInfo (String serviceName);
}
