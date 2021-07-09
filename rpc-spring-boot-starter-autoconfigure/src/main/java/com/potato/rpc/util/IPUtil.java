package com.potato.rpc.util;


import java.net.*;

public class IPUtil {
    public static String getIp(){
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
        return ip;
    }
}
