package com.hsx.manyue.modules.im.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.IMMessage;
import com.hsx.manyue.modules.im.model.dto.IMMessageDTO;
import com.hsx.manyue.modules.im.server.SessionManager;
import com.hsx.manyue.modules.im.service.IIMMessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket消息处理器
 */
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final SessionManager sessionManager;
    private final IIMMessageService messageService;

    public WebSocketFrameHandler(SessionManager sessionManager, IIMMessageService messageService) {
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("WebSocket连接建立: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            log.debug("收到WebSocket消息: {}", text);

            try {
                // 直接从JSON解析消息,不需要预先指定msgType
                IMMessage msg = JSONUtil.toBean(text, IMMessage.class);
                handleMessage(ctx, msg);
            } catch (Exception e) {
                log.error("解析WebSocket消息失败: {}", text, e);
            }
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, IMMessage msg) {
        try {
            switch (msg.getMsgType()) {
                case IMMessage.TYPE_AUTH:
                    handleAuth(ctx, msg);
                    break;
                case IMMessage.TYPE_HEARTBEAT:
                    handleHeartbeat(ctx, msg);
                    break;
                case IMMessage.TYPE_SINGLE_CHAT:
                    handleSingleChat(ctx, msg);
                    break;
                case IMMessage.TYPE_GROUP_CHAT:
                    handleGroupChat(ctx, msg);
                    break;
                case IMMessage.TYPE_ACK:
                    handleAck(ctx, msg);
                    break;
                case IMMessage.TYPE_READ_RECEIPT:
                    handleReadReceipt(ctx, msg);
                    break;
                case IMMessage.TYPE_TYPING:
                    handleTyping(ctx, msg);
                    break;
                case IMMessage.TYPE_FRIEND_APPLY:
                case IMMessage.TYPE_GROUP_NOTIFY:
                    // 仅记录或透传
                    log.debug("收到控制类消息: {}", msg.getMsgType());
                    break;
                default:
                    log.warn("未知消息类型: {}", msg.getMsgType());
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, IMMessage msg) {
        try {
            String token = msg.getToken();
            Long userId = JwtUtil.getIdOfPayload(token);
            
            if (userId != null) {
                sessionManager.bind(userId, ctx.channel());
                
                IMMessage response = new IMMessage();
                response.setMsgType(IMMessage.TYPE_AUTH);
                response.setData("认证成功");
                response.setMsgTime(System.currentTimeMillis());
                
                ctx.writeAndFlush(new TextWebSocketFrame(response.toJson()));
                log.info("WebSocket用户认证成功: userId={}", userId);
            } else {
                log.warn("WebSocket认证失败: 无效token");
                ctx.close();
            }
        } catch (Exception e) {
            log.error("WebSocket认证失败", e);
            ctx.close();
        }
    }

    private void handleHeartbeat(ChannelHandlerContext ctx, IMMessage msg) {
        IMMessage pong = IMMessage.createHeartbeat();
        ctx.writeAndFlush(new TextWebSocketFrame(pong.toJson()));
    }

    private void handleTyping(ChannelHandlerContext ctx, IMMessage msg) {
        // 转发正在输入状态给目标用户
        if (msg.getToUserId() != null) {
            sessionManager.pushMessage(msg.getToUserId(), msg);
        }
    }

    private void handleSingleChat(ChannelHandlerContext ctx, IMMessage msg) {
        IMMessageDTO dto = new IMMessageDTO();
        dto.setClientMsgId(msg.getClientMsgId());
        dto.setFromUserId(msg.getFromUserId());
        dto.setToUserId(msg.getToUserId());
        dto.setContent(msg.getContent());
        dto.setContentType(msg.getContentType());
        
        messageService.sendSingleMessage(dto);
    }

    private void handleGroupChat(ChannelHandlerContext ctx, IMMessage msg) {
        IMMessageDTO dto = new IMMessageDTO();
        dto.setClientMsgId(msg.getClientMsgId());
        dto.setFromUserId(msg.getFromUserId());
        dto.setToGroupId(msg.getToGroupId());
        dto.setContent(msg.getContent());
        dto.setContentType(msg.getContentType());
        
        messageService.sendGroupMessage(dto);
    }

    private void handleAck(ChannelHandlerContext ctx, IMMessage msg) {
        messageService.ackMessage(msg.getMsgSeq(), msg.getFromUserId());
    }

    private void handleReadReceipt(ChannelHandlerContext ctx, IMMessage msg) {
        messageService.readMessage(msg.getMsgSeq(), msg.getFromUserId());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (sessionManager != null) {
            sessionManager.unbind(ctx.channel());
        }
        log.info("WebSocket连接断开: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.warn("WebSocket读超时，关闭连接: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket连接异常: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
