package com.potato.rpc;

import com.potato.rpc.common.extension.ExtensionLoader;
import com.potato.rpc.loadbalance.LoadBalancer;

/**
 * LoadBalancer
 *
 * @author lizhifu
 * @date 2021/7/16
 */
public class LoadBalancerTest {
    public static void main(String[] args) {
        ExtensionLoader.getExtensionLoader(LoadBalancer.class).getExtension("random");
    }
}
