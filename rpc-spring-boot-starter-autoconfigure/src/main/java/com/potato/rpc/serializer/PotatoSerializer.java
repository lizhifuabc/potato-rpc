package com.potato.rpc.serializer;

import com.potato.rpc.common.extension.SPI;

/**
 * 序列化接口
 *
 * @author lizhifu
 * @date 2021/7/2
 */
@SPI
public interface PotatoSerializer {
    /**
     * 序列化（对象 -> 字节数组）
     *
     * @param object 需要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化（字节数组 -> 对象）
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>   类的类型
     * @return 反序列化之后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
