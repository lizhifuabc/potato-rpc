package com.potato.rpc.transport.netty.client;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty channel
 *
 * @author lizhifu
 * @date 2021/7/7
 */
public enum NettyClientChannelProvider {
    INSTANCE;
    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    public Channel get(String ipPort) {
        if (channelMap.containsKey(ipPort)) {
            Channel channel = channelMap.get(ipPort);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(ipPort);
            }
        }
        return null;
    }

    public void set(String ipPort, Channel channel) {
        channelMap.put(ipPort, channel);
    }

    public void remove(String ipPort) {
        channelMap.remove(ipPort);
    }
}
