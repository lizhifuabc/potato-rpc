package com.potato.rpc.serializer;


import com.potato.rpc.serializer.jdk.JDKSerializer;
import com.potato.rpc.serializer.kryo.KryoSerializer;

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
    public void setPotatoSerialize(String serializerType) {
        switch(serializerType){
            case "JDK" :
                this.potatoSerializer = new JDKSerializer();
                break;
            case "KRYO" :
                this.potatoSerializer = new KryoSerializer();
                break;
            default :
                this.potatoSerializer = new JDKSerializer();
        }
    }
}
