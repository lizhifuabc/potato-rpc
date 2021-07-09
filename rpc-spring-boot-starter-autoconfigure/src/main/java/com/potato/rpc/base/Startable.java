package com.potato.rpc.base;

/**
 * 执行接口
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public interface Startable {
    /**
     * 启动
     *
     * @return 是否启动成功
     */
    public boolean start();
}
