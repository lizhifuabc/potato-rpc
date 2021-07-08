package com.potato.rpc.serializer.jdk;

import com.potato.rpc.serializer.PotatoSerialize;
import com.potato.rpc.serializer.exception.SerializerException;
import org.apache.commons.lang.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * jdk序列化
 *
 * @author lizhifu
 * @date 2021/7/2
 */
public class JDKSerializer implements PotatoSerialize {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            throw new SerializerException("Object is null");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        byte[] bytes = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            bytes = baos.toByteArray();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {

            } finally {
                oos = null;
            }
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {

            } finally {
                baos = null;
            }

        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (ArrayUtils.isEmpty(bytes)) {
            throw new SerializerException("Bytes is null or empty");
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        Object object = null;
        try {
            ois = new ObjectInputStream(bais);
            object = ois.readObject();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {

            } finally {
                ois = null;
            }
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (Exception e) {

            } finally {
                bais = null;
            }
        }
        return (T) object;
    }
}
