package com.potato.rpc.serializer;


/**
 * 序列话工厂
 *
 * @author lizhifu
 * @date 2021/7/2
 */
public enum SerializerFactory {
    INSTANCE;
    private PotatoSerialize potatoSerialize;

    public <T> byte[] serialize(T object) {
        byte[] bytes = potatoSerialize.serialize(object);;
        return bytes;
    }
    public<T> T deserialize(byte[] bytes, Class<T> clazz) {
        return potatoSerialize.deserialize(bytes,clazz);
    }
    public PotatoSerialize getPotatoSerialize() {
        return potatoSerialize;
    }

    public void setPotatoSerialize(PotatoSerialize potatoSerialize) {
        this.potatoSerialize = potatoSerialize;
    }
}
