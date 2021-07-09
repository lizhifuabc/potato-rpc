package com.potato.rpc.transport.netty.client;

import com.potato.rpc.transport.netty.coder.NettyDecoder;
import com.potato.rpc.transport.netty.coder.NettyEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * netty initChannel
 *
 * @author lizhifu
 * @date 2021/7/6
 */
public class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        //5秒空闲发送心跳请求 NettyClientHandler userEventTriggered
        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
        p.addLast(new NettyEncoder());
        p.addLast(new NettyDecoder());
        p.addLast(new NettyClientHandlerAdapter());
    }
}
