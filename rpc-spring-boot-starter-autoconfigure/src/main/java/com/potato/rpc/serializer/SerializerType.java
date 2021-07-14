package com.potato.rpc.serializer;
/**
 * 序列化类型
 * @author lizhifu
 */
public enum SerializerType {
    /**
     * jdk
     */
    JDK((byte) 0x01, "jdk"),
    /**
     * kryo
     */
    KRYO((byte) 0x02, "kryo");

    private final byte code;
    private final String name;

    /**
     * 构造
     * @param code
     * @param name
     */
    SerializerType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取名称
     * @param code
     * @return
     */
    public static String getName(int code) {
        for (SerializerType c : SerializerType.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

    /**
     * 获取code
     * @param name
     * @return
     */
    public static byte getCode(String name) {
        for (SerializerType c : SerializerType.values()) {
            if (c.getName().equals(name)) {
                return c.getCode();
            }
        }
        return 0x01;
    }
    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
