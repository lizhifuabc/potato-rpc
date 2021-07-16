package com.potato.rpc.register.zk;

import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.config.RegistryConfig;
import com.potato.rpc.register.AbstractRegistry;
import com.potato.rpc.register.ProviderInfo;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * zk注册中心
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class ZookeeperRegistry extends AbstractRegistry {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);
    /**
     * ZooKeeper client
     */
    private CuratorFramework zkClient;

    public ZookeeperRegistry(RegistryConfig registryConfig){
        super(registryConfig);
    }

    @Override
    public synchronized boolean start() {
        if (zkClient == null) {
            throw new PotatoRuntimeException("ZookeeperRegistry not init");
        }
        if (zkClient.getState() == CuratorFrameworkState.STARTED) {
            return true;
        }
        try {
            zkClient.start();
        } catch (Exception e) {
            throw new PotatoRuntimeException("ZookeeperRegistry start exception", e);
        }
        return zkClient.getState() == CuratorFrameworkState.STARTED;
    }

    @Override
    public void register(ProviderInfo providerInfo) {
        try {
            providerInfoMap.putIfAbsent(providerInfo.getServiceName(),providerInfo);
            //创建service永久节点：/env/com.test.service
            String servicePath = "/".concat(providerInfo.getServiceName());
            Stat stat = zkClient().checkExists().forPath(servicePath);
            if(stat == null) {
                zkClient().create().forPath(servicePath,"".getBytes(StandardCharsets.UTF_8));
            }
            String childPath = servicePath + "/" + providerInfo.getIp() + ":" + providerInfo.getPort();
            Stat childStat = zkClient().checkExists().forPath(childPath);
            if(childStat == null) {
                zkClient().create().withMode(CreateMode.EPHEMERAL).forPath(childPath,providerInfo.json().getBytes(StandardCharsets.UTF_8));
            }
        }catch (Exception e){
            throw new PotatoRuntimeException("ZookeeperRegistry registry exception", e);
        }
    }
    @Override
    public void destroy() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            zkClient.close();
        }
        providerInfoMap.clear();
    }

    /**
     * zk客户端初始化
     */
    @Override
    public synchronized void init() {
        //保障只初始化一次
        if (zkClient != null) {
            return;
        }
        logger.info("ZookeeperRegistry init registryConfig:{}",registryConfig);
        //重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(registryConfig.getAddress())
                .sessionTimeoutMs(registryConfig.getConnectTimeout() * 3)
                .connectionTimeoutMs(registryConfig.getConnectTimeout())
                .namespace(registryConfig.getEnv())
                .canBeReadOnly(false)
                .retryPolicy(retryPolicy)
                .defaultData(null);
        zkClient = builder.build();
        //增加监听
        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState connectionState) {
                logger.info("ZookeeperRegistry connection state changed:{}",connectionState);
                if (connectionState == ConnectionState.RECONNECTED) {
                    recoverRegistryData();
                }
            }
        });
    }
    private void recoverRegistryData() {
        for (String serviceName : providerInfoMap.keySet()) {
            logger.info("ZookeeperRegistry recoverRegistryData serviceName {}",serviceName);
            register(providerInfoMap.get(serviceName));
        }
    }
    private CuratorFramework zkClient() {
        if (zkClient == null || zkClient.getState() != CuratorFrameworkState.STARTED) {
            throw new PotatoRuntimeException("ZookeeperRegistry zkClient check exception");
        }
        return zkClient;
    }
}
