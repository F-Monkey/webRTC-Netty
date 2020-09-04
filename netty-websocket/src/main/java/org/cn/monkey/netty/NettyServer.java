package org.cn.monkey.netty;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final ServerBootstrap bootstrap;

    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    private final int port;

    private final ChannelHandler customHandler;

    public NettyServer(int port,
                       ChannelHandler customHandler) {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
        this.port = port;
        Preconditions.checkNotNull(customHandler);
        this.customHandler = customHandler;
    }

    public void start() throws InterruptedException {
        WebSocketChannelInitializer webSocketChannelInitializer = new WebSocketChannelInitializer() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                super.initChannel(ch);
                ch.pipeline().addLast(NettyServer.this.customHandler);
            }
        };

        this.bootstrap.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(webSocketChannelInitializer);
        ChannelFuture channelFuture = this.bootstrap.bind(this.port).sync();
        log.info("netty server start at port: {}", this.port);

        try {
            channelFuture.channel().closeFuture().sync();
        } finally {
            this.bossGroup.shutdownGracefully();
            this.workerGroup.shutdownGracefully();
        }
    }
}
