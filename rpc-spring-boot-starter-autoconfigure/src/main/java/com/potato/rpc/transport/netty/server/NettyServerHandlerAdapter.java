package com.potato.rpc.transport.netty.server;

import com.potato.rpc.common.constants.PotatoRpcStatusEnum;
import com.potato.rpc.register.ProviderInfo;
import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.transport.model.RequestMessageType;
import com.potato.rpc.transport.model.ResponseMessageType;
import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.transport.model.RpcRequest;
import com.potato.rpc.transport.model.RpcResponse;
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
    private ServiceRegistry serviceRegistry;
    public NettyServerHandlerAdapter(ServiceRegistry serviceRegistry){
        this.serviceRegistry = serviceRegistry;
    }
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
                    //其他请求
                    rpcMessage.setMessageType(ResponseMessageType.RESPONSE_TYPE_NORMAL);
                    RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
                    //设置响应数据
                    RpcResponse response = new RpcResponse();
                    //获取服务信息
                    ProviderInfo providerInfo = serviceRegistry.getProviderInfo(rpcRequest.getServiceName());
                    //没有获取到服务对象
                    if(providerInfo == null){
                        response.setPotatoRpcStatusEnum(PotatoRpcStatusEnum.NOT_FOUND);
                    }else {
                        Method method = null;
                        try {
                            method = providerInfo.getObj().getClass().getMethod(rpcRequest.getMethod(), rpcRequest.getParameterTypes());
                            Object returnValue = method.invoke(providerInfo.getObj(), rpcRequest.getParameters());
                            response.setReturnValue(returnValue);
                            response.setPotatoRpcStatusEnum(PotatoRpcStatusEnum.SUCCESS);
                        } catch (NoSuchMethodException e) {
                            response.setException(e);
                            response.setPotatoRpcStatusEnum(PotatoRpcStatusEnum.NOT_FOUND);
                        }catch (Exception e){
                            response.setException(e);
                            response.setPotatoRpcStatusEnum(PotatoRpcStatusEnum.ERROR);
                        }
                    }
                    rpcMessage.setData(response);
                }
                //如果写入失败，则自动关闭通道
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
        logger.error("server catch exception",cause);
        cause.printStackTrace();
        ctx.close();
    }
}
