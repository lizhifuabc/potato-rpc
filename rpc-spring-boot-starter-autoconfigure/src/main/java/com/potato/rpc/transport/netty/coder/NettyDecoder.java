package com.potato.rpc.transport.netty.coder;

import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.transport.model.RpcMessage;
import com.potato.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解码器
 *
 * @author lizhifu
 * @date 2021/6/28
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * 发送的数据包最大长度
     */
    private final static int MAX_FRAME_LENGTH = 2 * 1024 * 1024;
    private final static Logger logger = LoggerFactory.getLogger(NettyDecoder.class);
    public NettyDecoder() {
        //（1） maxFrameLength - 发送的数据包最大长度；
        //（2） lengthFieldOffset - 长度域偏移量
        //（3） lengthFieldLength - 长度域的自己的字节数长度
        //（4） lengthAdjustment – 长度域的偏移量矫正。长度域的偏移量矫正。 如果长度域的值，除了包含有效数据域的长度外，还包含了其他域（如长度域自身）长度，那么，就需要进行矫正。矫正的值为：包长 - 长度域的值 – 长度域偏移 – 长度域长。
        //（5） initialBytesToStrip – 丢弃的起始字节数。全都是个人自定义校验，所以设置为0
        super(MAX_FRAME_LENGTH, 8, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 魔数
        int magic = in.readInt();
        //序列化类型
        int serializeType = in.readInt();
        //数据长度
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            throw new PotatoRuntimeException("body dataLength error");
        }
        //读取body数据
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        try {
            return SerializerFactory.INSTANCE.serverPotatoSerializer(serializeType).deserialize(data,RpcMessage.class);
        } catch (Exception ex) {
            logger.error("decode error: " + ex.toString());
        }
        return in;
    }
}
