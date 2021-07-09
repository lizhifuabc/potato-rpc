package com.potato.rpc.server;

import com.potato.rpc.annotation.PotatoRpcClient;
import com.potato.rpc.annotation.PotatoRpcServer;
import com.potato.rpc.properties.PotatoRpcConfigProperties;
import com.potato.rpc.transport.PotatoServer;
import com.potato.rpc.proxy.ClientProxyFactory;
import com.potato.rpc.register.ProviderInfo;
import com.potato.rpc.register.ServiceDiscovery;
import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.util.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 服务发布，暴露，自动注入Service
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class PotatoServerPublisher implements ApplicationListener<ContextRefreshedEvent> {
    private final static Logger logger = LoggerFactory.getLogger(PotatoServerPublisher.class);
    private PotatoServer potatoServer;
    private ServiceRegistry serviceRegistry;
    private ServiceDiscovery serviceDiscovery;
    private PotatoRpcConfigProperties potatoRpcConfigProperties;
    private ClientProxyFactory clientProxyFactory;
    /**
     * 初始
     * @param potatoServer server
     * @param serviceRegistry register
     */
    public PotatoServerPublisher(ClientProxyFactory clientProxyFactory, PotatoServer potatoServer, ServiceRegistry serviceRegistry, ServiceDiscovery serviceDiscovery, PotatoRpcConfigProperties potatoRpcConfigProperties){
        this.potatoServer = potatoServer;
        this.serviceRegistry = serviceRegistry;
        this.serviceDiscovery = serviceDiscovery;
        this.potatoRpcConfigProperties = potatoRpcConfigProperties;
        this.clientProxyFactory = clientProxyFactory;
    }
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //Spring启动完毕
        if (Objects.isNull(contextRefreshedEvent.getApplicationContext().getParent())){
            logger.info("spring init success");
            ApplicationContext context = contextRefreshedEvent.getApplicationContext();
            //开启服务
            startServer(context);
            //客户端
            try {
                startClient(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void startClient(ApplicationContext context) throws Exception {
        List<String> serviceList = new ArrayList<>();
        String[] names = context.getBeanDefinitionNames();
        for(String name : names){
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)){
                continue;
            }

            Field[] declaredFields = clazz.getDeclaredFields();
            for(Field field : declaredFields){
                PotatoRpcClient potatoRpcClient = field.getAnnotation(PotatoRpcClient.class);
                if (potatoRpcClient == null){
                    continue;
                }
                Class<?> fieldClass = field.getType();
                Object object = context.getBean(name);
                field.setAccessible(true);
                field.set(object,clientProxyFactory.getProxyInstance(fieldClass));
                serviceList.add(fieldClass.getName());
                serviceDiscovery.discovery(fieldClass.getName());
            }
        }
    }
    /**
     * 启动服务
     * @param context
     */
    private void startServer(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(PotatoRpcServer.class);
        if(beans.size() == 0){
            logger.info("no PotatoRpcService found");
            return;
        }
        String ip = IPUtil.getIp();
        for(Object obj : beans.values()){
            String serviceName = null;
            Class<?> clazz = obj.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            PotatoRpcServer service = clazz.getAnnotation(PotatoRpcServer.class);
            String value = service.value();
            if(value.equals("") && interfaces.length != 1){
                throw new UnsupportedOperationException("The exposed interface is not specific with '" + obj.getClass().getName() + "'");
            }
            if(value.equals("")){
                Class<?> supperClass = interfaces[0];
                serviceName = supperClass.getName();
            }else {
                serviceName = value;
            }

            ProviderInfo providerInfo = new ProviderInfo();
            providerInfo.setServiceName(serviceName);
            providerInfo.setIp(ip);
            providerInfo.setWeight(10);
            providerInfo.setEnable(1);
            providerInfo.setPort(potatoRpcConfigProperties.getPort());
            serviceRegistry.register(providerInfo);
        }
        potatoServer.start();
    }
}
