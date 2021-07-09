package com.potato.rpc.serializer;


/**
 * 序列话工厂
 *
 * @author lizhifu
 * @date 2021/7/2
 */
public enum SerializerFactory {
    INSTANCE;
    private PotatoSerializer potatoSerializer;

    public <T> byte[] serialize(T object) {
        byte[] bytes = potatoSerializer.serialize(object);;
        return bytes;
    }
    public<T> T deserialize(byte[] bytes, Class<T> clazz) {
        return potatoSerializer.deserialize(bytes,clazz);
    }
    public PotatoSerializer getPotatoSerialize() {
        return potatoSerializer;
    }

    public void setPotatoSerialize(PotatoSerializer potatoSerializer) {
        this.potatoSerializer = potatoSerializer;
    }
}
