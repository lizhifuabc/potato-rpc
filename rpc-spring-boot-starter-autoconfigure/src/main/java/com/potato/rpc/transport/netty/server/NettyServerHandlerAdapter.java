package com.potato.rpc.transport.netty.server;

import com.potato.rpc.transport.model.RequestMessageType;
import com.potato.rpc.transport.model.ResponseMessageType;
import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.transport.model.RpcRequest;
import com.potato.rpc.transport.model.RpcResponse;
import com.potato.rpc.common.model.ServiceObject;
import com.potato.rpc.server.cache.ServerRegistryCache;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * netty服务端
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class NettyServerHandlerAdapter extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandlerAdapter.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                logger.info("netty server receive message:{}", msg);
                RpcMessage rpcMessage = (RpcMessage) msg;
                if(rpcMessage.getMessageType() == RequestMessageType.REQUEST_TYPE_HEARTBEAT){
                    rpcMessage.setMessageType(ResponseMessageType.RESPONSE_TYPE_HEARTBEAT);
                    rpcMessage.setData("PONG");
                }else {
                    RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
                    ServiceObject serviceObject = ServerRegistryCache.SERVER_MAP.get(rpcRequest.getServiceName());
                    Method method = null;
                    try {
                        method = serviceObject.getObj().getClass().getMethod(rpcRequest.getMethod(), rpcRequest.getParameterTypes());
                        Object returnValue = method.invoke(serviceObject.getObj(), rpcRequest.getParameters());
                        RpcResponse response = new RpcResponse();
                        response.setReturnValue(returnValue);
                        rpcMessage.setMessageType(ResponseMessageType.RESPONSE_TYPE_NORMAL);
                        rpcMessage.setData(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                logger.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
