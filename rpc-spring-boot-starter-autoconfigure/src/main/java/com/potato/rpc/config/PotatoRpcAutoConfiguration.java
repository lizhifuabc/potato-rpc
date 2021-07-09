package com.potato.rpc.config;

import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.loadbalance.impl.RandomLoadBalance;
import com.potato.rpc.properties.PotatoRpcConfigProperties;
import com.potato.rpc.transport.PotatoClient;
import com.potato.rpc.transport.netty.client.NettyClient;
import com.potato.rpc.proxy.ClientProxyFactory;
import com.potato.rpc.register.RegistryFactory;
import com.potato.rpc.register.ServiceDiscovery;
import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.register.zk.ZkServiceDiscovery;
import com.potato.rpc.register.zk.ZookeeperRegistry;
import com.potato.rpc.serializer.SerializerFactory;
import com.potato.rpc.serializer.jdk.JDKSerializer;
import com.potato.rpc.transport.PotatoServer;
import com.potato.rpc.server.PotatoServerPublisher;
import com.potato.rpc.transport.netty.server.NettyServer;
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
        RegistryFactory.put(registryConfig,serviceRegistry);
        return serviceRegistry;
    }
    @Bean
    public PotatoServer nettyServer(@Autowired ServiceRegistry serviceRegistry) {
        return new NettyServer(potatoRpcConfigProperties.getPort(),serviceRegistry);
    }
    @Bean
    public ClientProxyFactory clientProxyFactory( @Autowired ServiceDiscovery serviceDiscovery) {
        PotatoClient potatoClient = new NettyClient();
        LoadBalancer loadBalancer = new RandomLoadBalance();
        return new ClientProxyFactory(potatoClient,loadBalancer,serviceDiscovery);
    }
    @Bean
    public ServiceDiscovery serviceDiscovery() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setEnv(potatoRpcConfigProperties.getEnv());
        registryConfig.setAddress(potatoRpcConfigProperties.getRegisterAddress());
        registryConfig.setConnectTimeout(potatoRpcConfigProperties.getConnectTimeout());
        ServiceDiscovery serviceDiscovery = new ZkServiceDiscovery(registryConfig);
        serviceDiscovery.init();
        serviceDiscovery.start();
        RegistryFactory.put(registryConfig,serviceDiscovery);
        return serviceDiscovery;
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
