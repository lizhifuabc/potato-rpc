package com.potato.rpc.register.zk;

import com.alibaba.fastjson.JSONObject;
import com.potato.rpc.client.cache.ServerDiscoveryCache;
import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.config.RegistryConfig;
import com.potato.rpc.register.AbstractDiscovery;
import com.potato.rpc.register.ProviderInfo;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务发现
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class ZkServiceDiscovery extends AbstractDiscovery {
    private final static Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);
    /**
     * ZooKeeper client
     */
    private CuratorFramework zkClient;

    public ZkServiceDiscovery(RegistryConfig registryConfig){
        super(registryConfig);
    }
    @Override
    public void destroy() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            zkClient.close();
        }
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
        logger.info("ZkServiceDiscovery init registryConfig:{}",registryConfig);
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
                logger.info("ZkServiceDiscovery connection state changed:{}",connectionState);
                if (connectionState == ConnectionState.RECONNECTED) {
                    recoverDiscoveryData();
                }
            }
        });
    }
    private void recoverDiscoveryData() {
        for (String serviceName : ServerDiscoveryCache.SERVER_MAP.keySet()) {
            discovery(serviceName);
        }
    }
    @Override
    public synchronized boolean start() {
        if (zkClient == null) {
            throw new PotatoRuntimeException("ZkServiceDiscovery not init");
        }
        if (zkClient.getState() == CuratorFrameworkState.STARTED) {
            return true;
        }
        try {
            zkClient.start();
        } catch (Exception e) {
            throw new PotatoRuntimeException("ZkServiceDiscovery start exception", e);
        }
        return zkClient.getState() == CuratorFrameworkState.STARTED;
    }
    private CuratorFramework zkClient() {
        if (zkClient == null || zkClient.getState() != CuratorFrameworkState.STARTED) {
            throw new PotatoRuntimeException("ZkServiceDiscovery zkClient check exception");
        }
        return zkClient;
    }

    @Override
    public void discovery(String serviceName) {
        try {
            //防止客户端先于服务端启动
            String servicePath = serviceName.startsWith("/")? serviceName : "/".concat(serviceName);
            Stat stat = zkClient().checkExists().forPath(servicePath);
            if(stat == null){
                zkClient().create().withMode(CreateMode.PERSISTENT).forPath(servicePath,"".getBytes(StandardCharsets.UTF_8));
            }
            //数据初始化
            List<String> childPathList = zkClient().getChildren().forPath(servicePath);
            updateProviderInfoList(childPathList,servicePath,serviceName);
            //开始创建监听
            CuratorCache curatorCache = CuratorCache.builder(zkClient(), servicePath).build();
            CuratorCacheListener listener = CuratorCacheListener.builder()
                    //一次父节点注册，监听每次子节点操作，不监听自身和查询
                    .forPathChildrenCache(servicePath, zkClient(), new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                            logger.info("ZkServerDiscovery PathChildrenCacheListener event：{}",event);
                            //子节点数据发生了变更
                            if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                                updateProviderInfo(event.getData(),serviceName);
                            }else {
                                List<String> childPathList = client.getChildren().forPath(servicePath);
                                logger.info("ZkServerDiscovery PathChildrenCacheListener childPathList：{}",childPathList);
                                //子节点发生了变更
                                updateProviderInfoList(childPathList,servicePath,serviceName);
                            }
                        }
                    })
                    .build();
            curatorCache.listenable().addListener(listener);
            curatorCache.start();
        }catch (Exception e){
            throw new PotatoRuntimeException("ZkServiceDiscovery discovery exception", e);
        }
    }
    private void updateProviderInfo(ChildData childData, String serviceName) {
        try {
            String data = new String(childData.getData(), "UTF-8");
            JSONObject json = JSONObject.parseObject(data);
            String _childPath = childData.getPath();
            //192.168.3.2:6666
            //远程ip
            String ip = _childPath.split(":")[0];
            //端口
            int port = Integer.valueOf(_childPath.split(":")[1]);
            //权重
            int weight = json.getInteger("weight");
            //是否可用
            int enable = json.getInteger("enable");
            ProviderInfo providerInfo = new ProviderInfo();
            providerInfo.setServiceName(serviceName);
            providerInfo.setIp(ip);
            providerInfo.setWeight(weight);
            providerInfo.setEnable(enable);
            providerInfo.setPort(port);
            logger.info("ZkServerDiscovery updateProviderInfo providerInfo:{}", providerInfo);
            List<ProviderInfo> providerInfoList = ServerDiscoveryCache.SERVER_MAP.get(serviceName);
            if(providerInfoList != null){
                for(int i = 0; i < providerInfoList.size(); i++) {
                    ProviderInfo _providerInfo = providerInfoList.get(i);
                    if(_providerInfo.getIp().equals(providerInfo.getIp()) && (_providerInfo.getPort() == providerInfo.getPort())) {
                        providerInfoList.set(i, providerInfo);
                        _providerInfo = null;
                    }
                }
            }
        } catch(UnsupportedEncodingException e) {
            throw new PotatoRuntimeException("ZkServiceDiscovery discovery updateProviderInfo exception", e);
        }
    }
    /**
     * 节点发生变更
     * @param childPathList
     * @param parentPath
     */
    private void updateProviderInfoList(List<String> childPathList, String parentPath, String serviceName) {
        try {
            ServerDiscoveryCache.SERVER_MAP.remove(serviceName);
            List<ProviderInfo> providerInfoList = new ArrayList<>();
            for(String _childPath : childPathList) {
                // /env/com.test.service/192.168.1.10:6666
                String currPath = parentPath.concat("/").concat(_childPath);
                byte[] bytes = zkClient().getData().forPath(currPath);
                String data = new String(bytes, "UTF-8");
                JSONObject json = JSONObject.parseObject(data);
                //ip
                String ip = _childPath.split(":")[0];
                //端口
                int port = Integer.valueOf(_childPath.split(":")[1]);
                //权重
                int weight = json.getInteger("weight");
                //是否可用
                int enable = json.getInteger("enable");
                ProviderInfo providerInfo = new ProviderInfo();
                providerInfo.setServiceName(serviceName);
                providerInfo.setIp(ip);
                providerInfo.setWeight(weight);
                providerInfo.setEnable(enable);
                providerInfo.setPort(port);
                providerInfoList.add(providerInfo);
            }
            ServerDiscoveryCache.SERVER_MAP.put(serviceName,providerInfoList);
        }catch (Exception e){
            throw new PotatoRuntimeException("ZkServiceDiscovery discovery updateProviderInfoList exception", e);
        }
    }
}
