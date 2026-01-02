package com.hsx.manyue.modules.im.server;

import com.hsx.manyue.modules.im.handler.TcpMessageHandler;
import com.hsx.manyue.modules.im.handler.WebSocketFrameHandler;
import com.hsx.manyue.modules.im.protocol.IMProtocolDecoder;
import com.hsx.manyue.modules.im.protocol.IMProtocolEncoder;
import com.hsx.manyue.modules.im.service.IIMMessageService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 协议检测处理器 - 区分WebSocket和TCP协议
 */
@Slf4j
public class ProtocolDetectionHandler extends ByteToMessageDecoder {

    private final String websocketPath;
    private final SessionManager sessionManager;
    private final IIMMessageService messageService;

    public ProtocolDetectionHandler(String websocketPath, SessionManager sessionManager, IIMMessageService messageService) {
        this.websocketPath = websocketPath;
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 需要至少能读取第一个字节来判断协议
        if (in.readableBytes() < 1) {
            return;
        }

        // 标记当前读位置
        in.markReaderIndex();
        byte firstByte = in.readByte();
        in.resetReaderIndex();

        ChannelPipeline pipeline = ctx.pipeline();

        // HTTP请求通常以'G'(GET)、'P'(POST)等开头
        if (firstByte == 'G' || firstByte == 'P' || firstByte == 'H') {
            // WebSocket协议
            log.info("检测到WebSocket协议连接: {}", ctx.channel().remoteAddress());
            setupWebSocketPipeline(pipeline);
        } else {
            // TCP协议
            log.info("检测到TCP协议连接: {}", ctx.channel().remoteAddress());
            setupTcpPipeline(pipeline);
        }

        // 移除自己
        pipeline.remove(this);
    }

    private void setupWebSocketPipeline(ChannelPipeline pipeline) {
        // HTTP编解码
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        
        // WebSocket协议处理
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true, 65536));
        
        // 心跳检测 (读超时90秒，写超时0，读写超时120秒)
        pipeline.addLast(new IdleStateHandler(90, 0, 120, TimeUnit.SECONDS));
        
        // WebSocket消息处理器
        pipeline.addLast("websocket-handler", new WebSocketFrameHandler(sessionManager, messageService));
    }

    private void setupTcpPipeline(ChannelPipeline pipeline) {
        // TCP消息编解码 (使用自定义协议)
        pipeline.addLast(new IMProtocolDecoder());
        pipeline.addLast(new IMProtocolEncoder());
        
        // 心跳检测
        pipeline.addLast(new IdleStateHandler(90, 0, 120, TimeUnit.SECONDS));
        
        // TCP消息处理器
        pipeline.addLast(new TcpMessageHandler(sessionManager, messageService));
    }
}

