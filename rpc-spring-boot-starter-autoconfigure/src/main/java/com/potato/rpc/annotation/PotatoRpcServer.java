package com.potato.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * rpc服务注解
 *
 * @author lizhifu
 * @date 2021/6/28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface PotatoRpcServer {
    String value() default "";
}
