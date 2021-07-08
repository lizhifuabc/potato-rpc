package com.potato.rpc.server;

import io.netty.util.internal.SystemPropertyUtil;

/**
 * 服务发布
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public class ServerPublisherConfig {
    public static final int DEFAULT_EVENT_LOOP_THREADS;
    public static final int DEFAULT_KOALAS_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1,SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
        DEFAULT_KOALAS_THREADS = 256;
    }
}
