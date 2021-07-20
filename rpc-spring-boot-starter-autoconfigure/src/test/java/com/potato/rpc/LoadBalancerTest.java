package com.potato.rpc;

import com.potato.rpc.common.extension.ExtensionLoader;
import com.potato.rpc.loadbalance.LoadBalancer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * LoadBalancer
 *
 * @author lizhifu
 * @date 2021/7/16
 */
public class LoadBalancerTest {
    private AtomicLong current = new AtomicLong(0);
    private int weight = 10;
    public static void main(String[] args) {
        ExtensionLoader.getExtensionLoader(LoadBalancer.class).getExtension("random");
    }
    @Test
    public void cal(){
        System.out.println(current.addAndGet(weight));
        System.out.println(current.addAndGet(weight));
        System.out.println(current.addAndGet(weight));

        System.out.println(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            long uptime = System.currentTimeMillis() - 1626667539013l;
            System.out.println(calculateWarmupWeight((int)uptime,1 * 60 * 1000,10));
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ( uptime / ((float) warmup / weight));
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }
}
