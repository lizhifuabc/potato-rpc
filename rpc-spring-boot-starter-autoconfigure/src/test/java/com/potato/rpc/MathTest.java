package com.potato.rpc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * MathTest
 *
 * @author lizhifu
 * @date 2021/7/20
 */
public class MathTest {
    private static AtomicLong current = new AtomicLong(0);
    public static void main(String[] args) {
        System.out.println(current.addAndGet(10));
        System.out.println(current.addAndGet(10));
        WeightedRoundRobin weightedRoundRobin = new WeightedRoundRobin();
        weightedRoundRobin.setWeight(10);
    }
    protected static class WeightedRoundRobin {
        // 服务提供者权重
        private int weight;
        // 当前权重
        private AtomicLong current = new AtomicLong(0);
        // 最后一次更新时间
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            // 初始情况下，current = 0
            current.set(0);
        }

        public long increaseCurrent() {
            // current = current + weight
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
}
