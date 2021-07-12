package com.potato.rpc.serializer;


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
    private PotatoSerializer potatoSerializer;
    private Map<Integer,PotatoSerializer> serverPotatoSerializer = new ConcurrentHashMap<>();
    private int serializerType;
    public PotatoSerializer serverPotatoSerializer(int serializerType){
        if(serverPotatoSerializer.get(serializerType) == null){
            switch(serializerType){
                case 1 :
                    serverPotatoSerializer.putIfAbsent(serializerType,new KryoSerializer());
                    break;
                default :
                    serverPotatoSerializer.putIfAbsent(serializerType,new JDKSerializer());
            }
        }
        return serverPotatoSerializer.get(serializerType);
    }
    public <T> byte[] serialize(T object) {
        byte[] bytes = potatoSerializer.serialize(object);;
        return bytes;
    }
    public int serializerType(){
        return serializerType;
    }
    public<T> T deserialize(byte[] bytes, Class<T> clazz) {
        return potatoSerializer.deserialize(bytes,clazz);
    }
    public synchronized void setPotatoSerialize(String serializerType) {
        switch(serializerType){
            case "KRYO" :
                this.potatoSerializer = new KryoSerializer();
                this.serializerType = 1;
                break;
            default :
                this.potatoSerializer = new JDKSerializer();
                this.serializerType = 0;
        }
    }
}
