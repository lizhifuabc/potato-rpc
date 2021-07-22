package com.potato.rpc.transport.netty.server;

import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.transport.netty.coder.NettyDecoder;
import com.potato.rpc.transport.netty.coder.NettyEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

/**
 * netty initChannel
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private EventExecutorGroup eventExecutorGroup;
    private ServiceRegistry serviceRegistry;
    public NettyServerChannelInitializer(EventExecutorGroup eventExecutorGroup,ServiceRegistry serviceRegistry){
        this.eventExecutorGroup = eventExecutorGroup;
        this.serviceRegistry = serviceRegistry;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //30秒之内没有收到客户端请求的话就关闭连接
        socketChannel.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        socketChannel.pipeline().addLast(new NettyDecoder());
        socketChannel.pipeline().addLast(new NettyEncoder());
        //创建一个EventExecutorGroup并将其和channelHandler绑定
        socketChannel.pipeline().addLast(eventExecutorGroup,new NettyServerHandler(serviceRegistry));
    }
}
