package com.potato.rpc.util;

import java.util.UUID;

/**
 * 随机数生成
 *
 * @author lizhifu
 * @date 2021/7/8
 */
public class RandomUtil {
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
