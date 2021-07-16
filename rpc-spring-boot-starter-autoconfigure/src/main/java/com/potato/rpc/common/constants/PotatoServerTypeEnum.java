package com.potato.rpc.common.constants;

/**
 * 启动类型
 *
 * @author lizhifu
 * @date 2021/7/16
 */
public enum PotatoServerTypeEnum {
    /**
     * ALL
     */
    ALL("客户端和服务端"),
    /**
     * CLIENT
     */
    CLIENT( "消费端"),
    /**
     * SERVER
     */
    SERVER("服务端");

    private String desc;

    PotatoServerTypeEnum(String desc){
        this.desc = desc;
    }
}
