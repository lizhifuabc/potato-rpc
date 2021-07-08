package com.potato.rpc.annotation;

import java.lang.annotation.*;

/**
 * 访问远程服务
 *
 * @author lizhifu
 * @date 2021/6/28
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PotatoRpcClient {
}
