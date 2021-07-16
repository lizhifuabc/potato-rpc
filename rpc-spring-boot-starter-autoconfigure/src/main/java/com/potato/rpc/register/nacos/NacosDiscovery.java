package com.potato.rpc.register.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.config.RegistryConfig;
import com.potato.rpc.register.AbstractDiscovery;
import com.potato.rpc.register.ProviderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * nacos
 *
 * @author lizhifu
 * @date 2021/7/15
 */
public class NacosDiscovery extends AbstractDiscovery {
    private final static Logger logger = LoggerFactory.getLogger(NacosDiscovery.class);
    private NamingService namingService;
    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    public NacosDiscovery(RegistryConfig registryConfig) {
        super(registryConfig);
    }

    @Override
    public void destroy() {
        if (namingService != null && namingService.getServerStatus() == "UP") {
            try {
                namingService.shutDown();
            } catch (NacosException e) {
                logger.error("NacosDiscovery destroy NacosException",e);
            }
        }
        SERVER_MAP.clear();
    }

    @Override
    public void init() {
        //保障只初始化一次
        if (namingService != null) {
            return;
        }
        try {
            namingService = NamingFactory.createNamingService(registryConfig.getAddress());
        } catch (NacosException e) {
            throw new PotatoRuntimeException("NacosDiscovery init NacosException",e);
        }
    }

    @Override
    public synchronized boolean start() {
        if (namingService == null) {
            throw new PotatoRuntimeException("NacosDiscovery not init");
        }
        if (namingService.getServerStatus() == "UP") {
            logger.info("NacosDiscovery start success:{}",registryConfig);
            return true;
        }
        if (namingService.getServerStatus() == "DOWN") {
            logger.info("NacosDiscovery start fail:{}",registryConfig);
            return false;
        }
        return false;
    }

    @Override
    public void discovery(String serviceName) {
        try {
            SERVER_MAP.remove(serviceName);
            List<ProviderInfo> providerInfoList = new ArrayList<>();
            List<Instance> instanceList = namingService.getAllInstances(serviceName,registryConfig.getEnv());
            instanceList.forEach(re->{
                ProviderInfo providerInfo = new ProviderInfo();
                providerInfo.setServiceName(serviceName);
                providerInfo.setIp(re.getIp());
                providerInfo.setWeight((int) re.getWeight());
                providerInfo.setEnable(re.isHealthy() == true ? 1:0 );
                providerInfo.setPort(re.getPort());
                providerInfoList.add(providerInfo);
            });
            namingService.subscribe(serviceName,registryConfig.getEnv(), event -> {
                logger.info("NamingEvent event:{}",event.toString());
                if (event instanceof NamingEvent) {
                    discovery(((NamingEvent) event).getServiceName());
                }
            });
            SERVER_MAP.put(serviceName,providerInfoList);
        } catch (NacosException e) {
            throw new PotatoRuntimeException("NacosDiscovery discovery exception", e);
        }
    }
}
