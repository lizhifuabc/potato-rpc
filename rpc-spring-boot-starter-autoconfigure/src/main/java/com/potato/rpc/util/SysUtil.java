package com.potato.rpc.util;

/**
 * 系统相关
 * @author lizhifu
 */
public class SysUtil {
    /**
     * CPU的核心数
     *
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
