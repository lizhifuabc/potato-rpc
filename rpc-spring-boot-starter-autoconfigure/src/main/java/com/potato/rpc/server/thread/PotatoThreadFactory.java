package com.potato.rpc.server.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public class PotatoThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public PotatoThreadFactory(String threadPoolName) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = threadPoolName + "-"
                + poolNumber.getAndIncrement()
                + "-thread-";
    }
    @Override
    public Thread newThread(Runnable r) {
        //创建的线程以“S-N-thread-M”命名，S是Service接口名，N是该工厂的序号，M是线程号
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY){
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
