package com.potato.rpc.transport.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * NettyEventLoopFactory
 *
 * @author lizhifu
 * @date 2021/7/21
 */
public class NettyEventLoopFactory {
    /**
     * TODO Epoll
     * EventLoopGroup创建
     * @param threads 线程数量
     * @param threadFactoryName 线程池名称
     * @return
     */
    public static EventLoopGroup eventLoopGroup(int threads, String threadFactoryName) {
        // 创建守护线程，jvm退出时能够正常退出
        ThreadFactory threadFactory = new DefaultThreadFactory(threadFactoryName, true);
        return new NioEventLoopGroup(threads, threadFactory);
    }
}
