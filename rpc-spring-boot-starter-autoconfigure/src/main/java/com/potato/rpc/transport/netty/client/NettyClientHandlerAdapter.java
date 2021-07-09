package com.potato.rpc.transport.netty.client;

import com.potato.rpc.transport.model.RequestMessageType;
import com.potato.rpc.transport.model.ResponseMessageType;
import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.transport.model.RpcResponse;
import com.potato.rpc.transport.netty.CompletableFutureHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * netty客户端
 *
 * @author lizhifu
 * @date 2021/7/6
 */
public class NettyClientHandlerAdapter extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(NettyClientHandlerAdapter.class);

    /**
     * 服务端返回的数据
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                logger.info("netty client receive message:{}", msg);
                RpcMessage rpcMessage = (RpcMessage) msg;
                if(rpcMessage.getMessageType() == ResponseMessageType.RESPONSE_TYPE_HEARTBEAT){
                    logger.info("netty client receive heartbeat message:{}", rpcMessage);
                }else {
                    CompletableFutureHelper.INSTANCE.complete(rpcMessage);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 对Netty心跳检测事件进行处理，几秒没有写操作的话，发送一个心跳信息过去，以防止服务端关闭ctx
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                logger.info("write idle happen [{}]", ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setMessageType(RequestMessageType.REQUEST_TYPE_HEARTBEAT);
                rpcMessage.setData("PING");
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 处理异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
