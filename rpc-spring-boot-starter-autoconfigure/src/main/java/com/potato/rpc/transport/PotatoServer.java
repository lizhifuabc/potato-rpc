package com.potato.rpc.transport;

/**
 * 服务
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public interface PotatoServer {
    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void stop();
}
