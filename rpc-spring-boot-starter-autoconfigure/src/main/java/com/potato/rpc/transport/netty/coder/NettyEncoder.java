package com.potato.rpc.transport.netty.coder;

import com.potato.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码、序列化
 +---------------------------------------------------------------+
 | 魔数 4byte |序列化类型 4byte |  数据长度 4byte  |
 +---------------------------------------------------------------+
 |                   数据内容 （长度不定）                          |
 +---------------------------------------------------------------+
 * @author lizhifu
 * @date 2021/6/28
 */
public class NettyEncoder extends MessageToByteEncoder{
    /**
     * 魔数6B
     */
    public static final int MAGIC_NUMBER = 0x1314;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] data = SerializerFactory.INSTANCE.serialize(o);
        //魔数:4B
        byteBuf.writeInt(MAGIC_NUMBER);
        //序列化类型：4B
        byteBuf.writeInt(SerializerFactory.INSTANCE.getByteSerializerType());
        //数据长度：4B
        byteBuf.writeInt(data.length);
        //发送的数据:长度不定
        byteBuf.writeBytes(data);
    }
}
