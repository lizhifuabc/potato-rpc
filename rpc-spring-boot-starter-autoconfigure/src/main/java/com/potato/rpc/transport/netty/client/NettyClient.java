package com.potato.rpc.transport.netty.client;

import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.transport.PotatoClient;
import com.potato.rpc.register.ProviderInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * netty客户端
 *
 * @author lizhifu
 * @date 2021/7/6
 */
public class NettyClient implements PotatoClient {
    private final static Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    /**
     * 初始化
     * TODO socks5
     */
    public NettyClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接超时时间:3s
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new NettyClientChannelInitializer());
    }
    @Override
    public CompletableFuture<RpcMessage> request(RpcMessage rpcMessage, ProviderInfo providerInfo) {
        CompletableFuture<RpcMessage> rpcFuture = new CompletableFuture<>();
        CompletableFutureHelper.INSTANCE.put(rpcMessage.getRequestId(),rpcFuture);

        String ipPort = providerInfo.getIp()+":"+providerInfo.getPort();
        Channel channel = getChannel(ipPort);
        if (channel.isActive()) {
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("client send rpcRequest:{}", rpcMessage);
                } else {
                    future.channel().close();
                    logger.error("client send failed:", future.cause());
                    CompletableFutureHelper.INSTANCE.remove(rpcMessage.getRequestId());
                    //这里直接抛异常
                    rpcFuture.completeExceptionally(future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return rpcFuture;
    }
    @SneakyThrows
    public Channel doConnect(String ipPort){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        String[] socketAddressArray = ipPort.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host,port);
        //建立与服务端的连接
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
    public Channel getChannel(String ipPort){
        Channel channel = NettyClientChannelProvider.INSTANCE.get(ipPort);
        if (channel == null) {
            channel = doConnect(ipPort);
            NettyClientChannelProvider.INSTANCE.set(ipPort, channel);
        }
        return channel;
    }
}
