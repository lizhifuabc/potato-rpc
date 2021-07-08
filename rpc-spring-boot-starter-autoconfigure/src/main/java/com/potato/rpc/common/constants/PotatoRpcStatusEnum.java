package com.potato.rpc.common.constants;

/**
 * 响应状态
 *
 * @author lizhifu
 * @date 2021/7/1
 */
public enum PotatoRpcStatusEnum {
    /**
     * SUCCESS
     */
    SUCCESS(200, "SUCCESS"),
    /**
     * ERROR
     */
    ERROR(500, "ERROR"),
    /**
     * NOT FOUND
     */
    NOT_FOUND(404, "NOT FOUND");

    private int code;

    private String desc;

    PotatoRpcStatusEnum(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
