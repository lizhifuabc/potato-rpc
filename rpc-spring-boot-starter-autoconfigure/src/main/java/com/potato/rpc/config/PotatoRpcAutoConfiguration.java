package com.potato.rpc.config;

import com.potato.rpc.client.discovery.ServiceDiscovery;
import com.potato.rpc.client.discovery.impl.zk.ZkServiceDiscovery;
import com.potato.rpc.client.discovery.impl.zk.ZkServerDiscoveryConfig;
import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.loadbalance.impl.RandomLoadBalance;
import com.potato.rpc.properties.PotatoRpcConfigProperties;
import com.potato.rpc.protocol.PotatoClient;
import com.potato.rpc.protocol.netty.client.NettyClient;
import com.potato.rpc.proxy.ClientProxyFactory;
import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.register.zk.ZookeeperRegistry;
import com.potato.rpc.serializer.SerializerFactory;
import com.potato.rpc.serializer.jdk.JDKSerializer;
import com.potato.rpc.protocol.PotatoServer;
import com.potato.rpc.server.PotatoServerPublisher;
import com.potato.rpc.protocol.netty.server.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 配置类
 *
 * @author lizhifu
 * @date 2021/6/25
 */
@Configuration
@EnableConfigurationProperties(PotatoRpcConfigProperties.class)
public class PotatoRpcAutoConfiguration {
    @Resource
    private PotatoRpcConfigProperties potatoRpcConfigProperties;
    @Bean
    public ServiceRegistry serviceRegistry() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setEnv(potatoRpcConfigProperties.getEnv());
        registryConfig.setAddress(potatoRpcConfigProperties.getRegisterAddress());
        registryConfig.setConnectTimeout(potatoRpcConfigProperties.getConnectTimeout());
        ServiceRegistry serviceRegistry = new ZookeeperRegistry(registryConfig);
        serviceRegistry.init();
        serviceRegistry.start();
        return serviceRegistry;
    }
    @Bean
    public PotatoServer nettyServer() {
        return new NettyServer(potatoRpcConfigProperties.getPort());
    }
    @Bean
    public ClientProxyFactory clientProxyFactory() {
        PotatoClient potatoClient = new NettyClient();
        LoadBalancer loadBalancer = new RandomLoadBalance();
        return new ClientProxyFactory(potatoClient,loadBalancer);
    }
    @Bean
    public ServiceDiscovery serviceDiscovery() {
        ZkServerDiscoveryConfig zkServerRegisterConfig = new ZkServerDiscoveryConfig();
        zkServerRegisterConfig.setZkAddress(potatoRpcConfigProperties.getRegisterAddress());
        zkServerRegisterConfig.setWeight(potatoRpcConfigProperties.getWeight());
        zkServerRegisterConfig.setEnv(potatoRpcConfigProperties.getEnv());
        zkServerRegisterConfig.setPort(potatoRpcConfigProperties.getPort());
        zkServerRegisterConfig.setServer(potatoRpcConfigProperties.getServer());
        ZkServiceDiscovery zkServiceDiscovery = new ZkServiceDiscovery(zkServerRegisterConfig);
        zkServiceDiscovery.init();
        return zkServiceDiscovery;
    }
    @Bean
    public PotatoServerPublisher serverPublisher(@Autowired PotatoServer potatoServer,
                                                 @Autowired ServiceRegistry serviceRegistry,
                                                 @Autowired ServiceDiscovery serviceDiscovery,
                                                 @Autowired PotatoRpcConfigProperties potatoRpcConfigProperties,
                                                 @Autowired ClientProxyFactory clientProxyFactory) {
        //设置序列化方式
        SerializerFactory.INSTANCE.setPotatoSerialize(new JDKSerializer());
        return new PotatoServerPublisher(clientProxyFactory,potatoServer, serviceRegistry,serviceDiscovery,potatoRpcConfigProperties);
    }
}
