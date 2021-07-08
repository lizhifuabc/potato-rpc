package com.potato.rpc.common.exception;

/**
 * 异常
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class PotatoRuntimeException extends RuntimeException{
    protected PotatoRuntimeException() {

    }

    public PotatoRuntimeException(String message) {
        super(message);
    }

    public PotatoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PotatoRuntimeException(Throwable cause) {
        super(cause);
    }
}
