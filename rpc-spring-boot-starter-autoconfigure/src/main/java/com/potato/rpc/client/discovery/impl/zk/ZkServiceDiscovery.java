package com.potato.rpc.client.discovery.impl.zk;

import com.alibaba.fastjson.JSONObject;
import com.potato.rpc.client.cache.ServerDiscoveryCache;
import com.potato.rpc.client.discovery.impl.AbstractServiceDiscovery;
import com.potato.rpc.common.model.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * zk 服务发现中心
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class ZkServiceDiscovery extends AbstractServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);
    /**
     * ZooKeeper
     */
    private CuratorFramework client;
    public static final String UTF_8 = "UTF-8";
    /**
     * zk配置
     */
    private ZkServerDiscoveryConfig zkServerDiscoveryConfig;

    public ZkServiceDiscovery(ZkServerDiscoveryConfig zkServerDiscoveryConfig){
        this.zkServerDiscoveryConfig = zkServerDiscoveryConfig;
    }
    public void init(){

    }
    @Override
    public void discovery(List<String> serviceList) throws Exception{
        logger.info("ZkServerDiscovery connected start zkServerDiscoveryConfig:{}",zkServerDiscoveryConfig);
        //创建环境永久节点：/env
        String envPath = zkServerDiscoveryConfig.getEnv().startsWith("/") ? zkServerDiscoveryConfig.getEnv().replace("/","") : zkServerDiscoveryConfig.getEnv();

        client = CuratorFrameworkFactory.builder()
                .namespace(envPath)
                .retryPolicy(new RetryNTimes(zkServerDiscoveryConfig.getRetryCount(),zkServerDiscoveryConfig.getSleepMsBetweenRetries()))
                .connectString(zkServerDiscoveryConfig.getZkAddress())
                .build();

        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if(connectionState == ConnectionState.LOST){
                    logger.info("ZkServerDiscovery connected lost zkServerDiscoveryConfig:{}",zkServerDiscoveryConfig);
                }
                if(connectionState == ConnectionState.CONNECTED){
                    logger.info("ZkServerDiscovery connected connected zkServerDiscoveryConfig:{}",zkServerDiscoveryConfig);
                }
                if(connectionState == ConnectionState.RECONNECTED){
                    logger.info("ZkServerDiscovery connected reconnected zkServerDiscoveryConfig:{}",zkServerDiscoveryConfig);
                    ZkServiceDiscovery.this.destroy();
                    try {
                        discovery(serviceList);
                    } catch (Exception e) {
                        logger.error("ZkServerDiscovery reconnected failed",e);
                    }
                }
            }
        });
        client.start();
        //重试策略执行完成之后，还是无法连接zk
        if(client == null) {
            logger.info("ZkServerDiscovery connected failed zkServerDiscoveryConfig:{}",zkServerDiscoveryConfig);
            throw new IllegalArgumentException("ZkServerDiscovery connected failed");
        }
        logger.info("ZkServerDiscovery connected success zkServerDiscoveryConfig:{}",zkServerDiscoveryConfig);
        for (int i = 0; i < serviceList.size(); i++) {
            String serviceName = serviceList.get(i);
            //防止客户端先于服务端启动
            String servicePath = serviceName.startsWith("/")? serviceName : "/".concat(serviceName);
            Stat stat = client.checkExists().forPath(servicePath);
            if(stat == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath,"".getBytes(StandardCharsets.UTF_8));
            }
            //数据初始化
            List<String> childPathList = client.getChildren().forPath(servicePath);
            updateServerList(childPathList,servicePath,serviceName);

            //开始创建监听
            CuratorCache curatorCache = CuratorCache.builder(client, servicePath).build();
            CuratorCacheListener listener = CuratorCacheListener.builder()
                    //一次父节点注册，监听每次子节点操作，不监听自身和查询
                    .forPathChildrenCache(servicePath, client, new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                            logger.info("ZkServerDiscovery PathChildrenCacheListener event：{}",event);
                            //子节点数据发生了变更
                            if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                                updateServer(event.getData(),serviceName);
                            }else {
                                List<String> childPathList = client.getChildren().forPath(servicePath);
                                logger.info("ZkServerDiscovery PathChildrenCacheListener childPathList：{}",childPathList);
                                //子节点发生了变更
                                updateServerList(childPathList,servicePath,serviceName);
                            }
                        }
                    })
                    .build();
            curatorCache.listenable().addListener(listener);
            curatorCache.start();
        }
    }
    private void updateServer(ChildData childData,String serviceName) {
        try {
            String data = new String(childData.getData(), UTF_8);
            JSONObject json = JSONObject.parseObject(data);
            String _childPath = childData.getPath();
            //192.168.3.2:6666
            //远程ip
            String ip = _childPath.split(":")[0];
            //服务端口
            String port = _childPath.split(":")[1];
            //权重
            String weight = json.getString("weight");
            //是否可用
            String enable = json.getString("enable");
            //注冊类型
            String server = json.getString("server");
            ServerInfo serverInfo = new ServerInfo(ip, port, Integer.valueOf(weight), "1".equals(enable), server);
            logger.info("ZkServerDiscovery updateServer remoteServerInfo:{}", serverInfo);
            List<ServerInfo> serverList = ServerDiscoveryCache.SERVER_MAP.get(serviceName);
            if(serverList != null){
                for(int i = 0; i < serverList.size(); i++) {
                    ServerInfo tempServer_ = serverList.get(i);
                    if(tempServer_.getIp().equals(serverInfo.getIp()) && tempServer_.getPort().equals(serverInfo.getPort())) {
                        serverList.set(i, serverInfo);
                        tempServer_ = null;
                    }
                }
            }
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    /**
     * 关闭zk
     */
    public void destroy(){
        if (client != null) {
            client.close();
            client = null;
        }
    }
    /**
     * 节点数据发生变更
     * @param childPathList
     * @param parentPath
     */
    private void updateServerList(List<String> childPathList, String parentPath,String serviceName) {
        ServerDiscoveryCache.SERVER_MAP.remove(serviceName);
        List<ServerInfo> serverInfoList = new ArrayList<>();
        for(String _childPath : childPathList) {
            // /env/com.test.service/192.168.1.10:6666
            String currPath = parentPath.concat("/").concat(_childPath);
            try {
                byte[] bytes = client.getData().forPath(currPath);
                String data = new String(bytes, "UTF-8");
                JSONObject json = JSONObject.parseObject(data);
                //远程ip
                String ip = _childPath.split(":")[0];
                //服务端口
                String port = _childPath.split(":")[1];
                //权重
                String weight = json.getString("weight");
                //是否可用
                String enable = json.getString("enable");
                //注冊类型
                String server = json.getString("server");
                ServerInfo serverInfo = new ServerInfo(ip, port, Integer.valueOf(weight), "1".equals(enable), server);
                serverInfoList.add(serverInfo);
            } catch(KeeperException e) {
                logger.error(e.getMessage()+ "currPath is not exists!", e);
            } catch(Exception e) {
                logger.error(e.getMessage()+ "the current thread is Interrupted", e);
            }
        }
        ServerDiscoveryCache.SERVER_MAP.put(serviceName,serverInfoList);
    }
}
