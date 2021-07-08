package com.potato.rpc.proxy;

import com.potato.rpc.client.cache.ServerDiscoveryCache;
import com.potato.rpc.client.discovery.ServiceDiscovery;
import com.potato.rpc.common.constants.RequestMessageType;
import com.potato.rpc.common.model.RpcMessage;
import com.potato.rpc.common.model.RpcRequest;
import com.potato.rpc.common.model.RpcResponse;
import com.potato.rpc.common.model.ServerInfo;
import com.potato.rpc.loadbalance.LoadBalancer;
import com.potato.rpc.protocol.PotatoClient;
import com.potato.rpc.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private Map<Class<?>, Object> objectCache = new HashMap<>();

    public ClientProxyFactory(PotatoClient potatoClient,LoadBalancer loadBalancer){
        this.potatoClient = potatoClient;
        this.loadBalancer = loadBalancer;
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
            List<ServerInfo> list = ServerDiscoveryCache.SERVER_MAP.get(serviceName);
            ServerInfo serviceInfo = loadBalancer.select(list);
            logger.info("get serviceName {} serviceInfo {}",serviceName,serviceInfo);
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(RandomUtil.uuid());
            rpcRequest.setMethod(method.getName());
            rpcRequest.setServiceName(serviceName);
            rpcRequest.setParameters(args);
            rpcRequest.setParameterTypes(method.getParameterTypes());
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setData(rpcRequest);
            rpcMessage.setMessageType(RequestMessageType.REQUEST_TYPE_NORMAL);
            CompletableFuture<RpcResponse> completableFuture = potatoClient.request(rpcMessage,serviceInfo);
            return completableFuture.get().getReturnValue();
        }
    }
}
