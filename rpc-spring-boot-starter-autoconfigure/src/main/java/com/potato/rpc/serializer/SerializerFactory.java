package com.potato.rpc.serializer;


import com.potato.rpc.common.extension.ExtensionLoader;
import com.potato.rpc.serializer.jdk.JDKSerializer;
import com.potato.rpc.serializer.kryo.KryoSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列话工厂
 *
 * @author lizhifu
 * @date 2021/7/2
 */
public enum SerializerFactory {
    /**
     * INSTANCE
     */
    INSTANCE;
    private String serializerType;
    public <T> byte[] serialize(T object) {
        return ExtensionLoader.getExtensionLoader(PotatoSerializer.class).getExtension(serializerType).serialize(object);
    }
    public<T> T deserialize(byte[] bytes, Class<T> clazz,int serializerType) {
        return ExtensionLoader.getExtensionLoader(PotatoSerializer.class).getExtension(SerializerType.getName(serializerType)).deserialize(bytes,clazz);
    }
    public synchronized void setPotatoSerialize(String serializerType) {
        this.serializerType = serializerType;
    }

    public String getSerializerType() {
        return serializerType;
    }
    public byte getByteSerializerType() {
       return SerializerType.getCode(serializerType);
    }
}
