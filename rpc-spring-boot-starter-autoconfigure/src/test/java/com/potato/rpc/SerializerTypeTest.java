package com.potato.rpc;

import com.potato.rpc.serializer.SerializerType;

/**
 * SerializerType
 *
 * @author lizhifu
 * @date 2021/7/14
 */
public class SerializerTypeTest {
    public static void main(String[] args) {
        System.out.println(SerializerType.getCode("kryo"));
    }
}
