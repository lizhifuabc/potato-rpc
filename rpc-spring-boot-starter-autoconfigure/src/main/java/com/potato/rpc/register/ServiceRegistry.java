package com.potato.rpc.register;

/**
 * 服务注册中心
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public interface ServiceRegistry {
    /**
     * 初始化
     */
    public void init();
    /**
     * 启动
     *
     * @return 是否启动成功
     */
    public boolean start();
    /**
     * 服务注册
     *
     * @param providerInfo 服务注册信息
     */
    public void register(ProviderInfo providerInfo);
    /**
     * 销毁接口
     */
    public void destroy();
}
