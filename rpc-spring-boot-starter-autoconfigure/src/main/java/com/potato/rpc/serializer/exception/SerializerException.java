package com.potato.rpc.serializer.exception;

/**
 * 自定义异常：序列化异常
 *
 * @author lizhifu
 * @date 2021/7/2
 */
public class SerializerException extends RuntimeException{
    public SerializerException() {
        super();
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerException(Throwable cause) {
        super(cause);
    }
}
