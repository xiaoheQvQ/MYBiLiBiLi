package com.hsx.manyue.modules.im.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * Netty服务器启动类 - 支持TCP和WebSocket双协议
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServerBootstrap {

    @Value("${netty.server.port:9000}")
    private int port;

    @Value("${netty.server.websocket.path:/ws/im}")
    private String websocketPath;

    private final SessionManager sessionManager;
    private final com.hsx.manyue.modules.im.service.IIMMessageService messageService;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                startServer();
            } catch (InterruptedException e) {
                log.error("Netty服务器启动失败", e);
            }
        }, "netty-server").start();
    }

    private void startServer() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 协议检测器 - 自动识别HTTP(WebSocket)或TCP
                            ch.pipeline().addLast(new ProtocolDetectionHandler(websocketPath, sessionManager, messageService));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            channel = future.channel();
            log.info("✅ Netty IM服务器启动成功，端口: {}, WebSocket路径: {}", port, websocketPath);

            // 等待服务器关闭
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("Netty IM服务器启动失败", e);
        } finally {
            shutdown();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        log.info("Netty IM服务器已关闭");
    }
}
