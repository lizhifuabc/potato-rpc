package com.potato.rpc.transport.netty.server;

import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.transport.PotatoServer;
import com.potato.rpc.server.ServerPublisherConfig;
import com.potato.rpc.util.SysUtil;
import com.potato.rpc.util.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty
 *
 * @author lizhifu
 * @date 2021/6/25
 */
public class NettyServer implements PotatoServer {
    private final static Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup serviceHandlerGroup;
    private ServiceRegistry serviceRegistry;
    private int port;
    public NettyServer(int port,ServiceRegistry serviceRegistry){
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }
    @Override
    public void start() {
        try {
            if(Epoll.isAvailable()) {
                logger.info("netty server bossGroup workerGroup is epoll");
                bossGroup = new EpollEventLoopGroup(ServerPublisherConfig.DEFAULT_EVENT_LOOP_THREADS);
                workerGroup = new EpollEventLoopGroup(ServerPublisherConfig.DEFAULT_EVENT_LOOP_THREADS * 2);
            } else {
                logger.info("netty server bossGroup workerGroup is common");
                bossGroup = new NioEventLoopGroup(ServerPublisherConfig.DEFAULT_EVENT_LOOP_THREADS);
                workerGroup = new NioEventLoopGroup(ServerPublisherConfig.DEFAULT_EVENT_LOOP_THREADS * 2);
            }
            serviceHandlerGroup = new DefaultEventExecutorGroup(
                    SysUtil.cpus() * 2,
                    ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
            );
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(workerGroup instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new NettyServerChannelInitializer(serviceHandlerGroup,serviceRegistry))
                    // 临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    // 如果未设置或所设置的值小于1，Java将使用默认值50。
                    // 如果大于队列的最大长度，请求会被拒绝
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    // 是否开启 TCP 底层心跳机制
                    .option(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，同步等待绑定成功
            Channel ch = b.bind(port).sync().channel();
            //jvm中增加一个关闭的钩子 TODO 是否增加zk销毁
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    shutdown();
                }
            });
        }catch (Exception e){
            logger.error ( "netty server start failed",e );
            if(bossGroup != null){
                bossGroup.shutdownGracefully();
            }
            if(workerGroup != null){
                workerGroup.shutdownGracefully();
            }
            if(serviceHandlerGroup != null){
                serviceHandlerGroup.shutdownGracefully();
            }
        }
        logger.info("netty server init success");
    }

    @Override
    public void stop() {
        shutdown();
    }
    private void shutdown(){
        if(bossGroup != null){
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null){
            workerGroup.shutdownGracefully();
        }
        if(serviceHandlerGroup != null){
            serviceHandlerGroup.shutdownGracefully();
        }
        logger.info("netty server shutdown success");
    }
}
