package com.potato.rpc.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

/**
 * 线程池工具类
 *
 * @author lizhifu
 * @date 2021/7/6
 */
public class ThreadPoolFactoryUtil {
    /**
     * 创建线程池
     *
     * @param threadNamePrefix 线程名字的前缀
     * @param daemon           守护线程
     * @return 线程池
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, boolean daemon) {
        return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
    }
}
