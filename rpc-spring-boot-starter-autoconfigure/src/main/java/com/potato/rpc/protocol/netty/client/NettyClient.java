package com.potato.rpc.protocol.netty.client;

import com.potato.rpc.common.model.RpcMessage;
import com.potato.rpc.common.model.RpcRequest;
import com.potato.rpc.common.model.RpcResponse;
import com.potato.rpc.common.model.ServerInfo;
import com.potato.rpc.protocol.PotatoClient;
import com.potato.rpc.protocol.netty.CompletableFutureHelper;
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
     */
    public NettyClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new NettyClientChannelInitializer());
    }
    @Override
    public CompletableFuture<RpcResponse> request(RpcMessage rpcMessage, ServerInfo serviceInfo) {
        RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
        CompletableFuture<RpcResponse> rpcFuture = new CompletableFuture<>();
        CompletableFutureHelper.INSTANCE.put(rpcRequest.getRequestId(),rpcFuture);
        String ipPort = serviceInfo.getIp()+":"+serviceInfo.getPort();
        Channel channel = getChannel(ipPort);
        if (channel.isActive()) {
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("client send rpcRequest:{}", rpcRequest);
                } else {
                    future.channel().close();
                    logger.error("client send failed:", future.cause());
                    CompletableFutureHelper.INSTANCE.remove(rpcRequest.getRequestId());
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
