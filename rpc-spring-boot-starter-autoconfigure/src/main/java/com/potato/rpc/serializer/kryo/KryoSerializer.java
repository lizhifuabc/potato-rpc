package com.potato.rpc.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.potato.rpc.common.exception.PotatoRuntimeException;
import com.potato.rpc.serializer.PotatoSerializer;
import com.potato.rpc.transport.model.RpcMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo 序列化
 *
 * @author lizhifu
 * @date 2021/7/9
 */
public class KryoSerializer implements PotatoSerializer {
    /**
     * kryo不是线程安全的，所以每个线程都使用独立的kryo
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcMessage.class);
        return kryo;
    });
    @Override
    public byte[] serialize(Object object) {
        //try-with-resource语句，该语句确保了每个资源,在语句结束时关闭
        try ( ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              Output output = new Output(byteArrayOutputStream)){
            Kryo kryo = kryoThreadLocal.get();
            //将对象序列化为byte数组
            kryo.writeObject(output, object);
            //删除，防止内存溢出
            kryoThreadLocal.remove();
            return output.toBytes();
        }catch (Exception e){
            throw new PotatoRuntimeException("Kryo serialize Exception",e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        //try-with-resource语句，该语句确保了每个资源,在语句结束时关闭
        try ( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
              Input input = new Input(byteArrayInputStream)){
            Kryo kryo = kryoThreadLocal.get();
            //反序列化出对对象
            Object o = kryo.readObject(input, clazz);
            //删除，防止内存溢出
            kryoThreadLocal.remove();
            return clazz.cast(o);
        }catch (Exception e){
            throw new PotatoRuntimeException("Kryo deserialize Exception",e);
        }
    }
}
