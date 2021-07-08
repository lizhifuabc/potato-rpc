package com.potato.rpc.protocol.netty.coder;

import com.potato.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

/**
 * 编码、序列化
 * 长度(4)包id(4)版本号(1)压缩类型(1)内容(n)包尾(2)
 * @author lizhifu
 * @date 2021/6/28
 */
public class NettyEncoder extends MessageToByteEncoder{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] data = SerializerFactory.INSTANCE.serialize(o);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
