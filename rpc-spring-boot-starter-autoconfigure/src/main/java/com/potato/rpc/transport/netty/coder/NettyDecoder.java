package com.potato.rpc.transport.netty.coder;

import com.potato.rpc.common.model.RpcResponse;
import com.potato.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 解码器
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class NettyDecoder extends ByteToMessageDecoder {
    private final static Logger logger = LoggerFactory.getLogger(NettyDecoder.class);
    /**
     * 数据包基础长度
     */
    private static final int BASE_LENGTH = 4;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < BASE_LENGTH) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = null;
        try {
            obj = SerializerFactory.INSTANCE.deserialize(data, RpcResponse.class);
            out.add(obj);
        } catch (Exception ex) {
            logger.error("decode error: " + ex.toString());
        }
    }
}
