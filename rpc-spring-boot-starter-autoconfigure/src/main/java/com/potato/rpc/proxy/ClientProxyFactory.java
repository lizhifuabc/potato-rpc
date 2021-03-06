package com.potato.rpc.proxy;

import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.common.extension.ExtensionLoader;
import com.potato.rpc.config.PotatoRpcConfigProperties;
import com.potato.rpc.register.ServiceDiscovery;
import com.potato.rpc.serializer.SerializerFactory;
import com.potato.rpc.transport.model.RequestMessageType;
import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.transport.model.RpcRequest;
import com.potato.rpc.transport.model.RpcResponse;
import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.transport.PotatoClient;
import com.potato.rpc.register.ProviderInfo;
import com.potato.rpc.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 客户端代理工厂
 *
 * @author lizhifu
 * @date 2021/6/29
 */
public class ClientProxyFactory{
    private final Logger logger = LoggerFactory.getLogger(ClientProxyFactory.class);
    private PotatoClient potatoClient;
    private LoadBalancer loadBalancer;
    private ServiceDiscovery serviceDiscovery;
    private Map<Class<?>, Object> objectCache = new HashMap<>();
    private PotatoRpcConfigProperties potatoRpcConfigProperties;
    public ClientProxyFactory(PotatoClient potatoClient, PotatoRpcConfigProperties potatoRpcConfigProperties, ServiceDiscovery serviceDiscovery){
        this.potatoClient = potatoClient;
        this.serviceDiscovery = serviceDiscovery;
        this.potatoRpcConfigProperties = potatoRpcConfigProperties;
        this.loadBalancer = ExtensionLoader.getExtensionLoader(LoadBalancer.class).getExtension(potatoRpcConfigProperties.getLoadBalance());
    }
    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxyInstance(Class<T> clazz) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz))
        );
    }
    private class ClientInvocationHandler implements InvocationHandler {
        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }
        /**
         *
         * @param proxy 当前对象，即代理对象，在调用谁的方法
         * @param method 当前被调用的方法（目标方法）
         * @param args 方法实参
         * @return 执行返回值
         * @throws Throwable
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }
            if (method.getName().equals("hashCode")) {
                return 0;
            }
            logger.info("proxy: {}", proxy.getClass().getName());
            logger.info("method: {}", method);
            // 1.获得服务信息
            String serviceName = clazz.getName();
            List<ProviderInfo> list = serviceDiscovery.getProviderInfo(serviceName);
            ProviderInfo providerInfo = loadBalancer.select(list);
            if(providerInfo == null){
                throw new PotatoRuntimeException("no provider found");
            }
            logger.info("get serviceName {} providerInfo {}",serviceName,providerInfo);
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setMethod(method.getName());
            rpcRequest.setServiceName(serviceName);
            rpcRequest.setParameters(args);
            rpcRequest.setParameterTypes(method.getParameterTypes());

            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setData(rpcRequest);
            rpcMessage.setRequestId(RandomUtil.uuid());
            rpcMessage.setMessageType(RequestMessageType.REQUEST_TYPE_NORMAL);

            CompletableFuture<RpcMessage> completableFuture = potatoClient.request(rpcMessage,providerInfo);
            RpcResponse rpcResponse = (RpcResponse) completableFuture.get(3, TimeUnit.SECONDS).getData();
            return rpcResponse.getReturnValue();
        }
    }
}
