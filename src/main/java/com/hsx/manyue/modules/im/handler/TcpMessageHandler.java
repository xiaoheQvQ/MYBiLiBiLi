package com.hsx.manyue.modules.im.handler;

import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.IMMessage;
import com.hsx.manyue.modules.im.model.dto.IMMessageDTO;
import com.hsx.manyue.modules.im.server.SessionManager;
import com.hsx.manyue.modules.im.service.IIMMessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP消息处理器
 */
@Slf4j
public class TcpMessageHandler extends SimpleChannelInboundHandler<IMMessage> {

    private final SessionManager sessionManager;
    private final IIMMessageService messageService;

    public TcpMessageHandler(SessionManager sessionManager, IIMMessageService messageService) {
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("TCP连接建立: {}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) {
        log.debug("收到TCP消息: type={}, from={}", msg.getMsgType(), msg.getFromUserId());

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
                default:
                    log.warn("未知消息类型: {}", msg.getMsgType());
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    /**
     * 处理认证
     */
    private void handleAuth(ChannelHandlerContext ctx, IMMessage msg) {
        try {
            String token = msg.getToken();
            Long userId = JwtUtil.getIdOfPayload(token);
            
            if (userId != null) {
                // 绑定用户和Channel
                sessionManager.bind(userId, ctx.channel());
                
                // 发送认证成功响应
                IMMessage response = new IMMessage();
                response.setMsgType(IMMessage.TYPE_AUTH);
                response.setData("认证成功");
                response.setMsgTime(System.currentTimeMillis());
                ctx.writeAndFlush(response);
                
                log.info("用户认证成功: userId={}, channel={}", userId, ctx.channel().id().asShortText());
            } else {
                log.warn("认证失败: 无效token");
                ctx.close();
            }
        } catch (Exception e) {
            log.error("认证失败", e);
            ctx.close();
        }
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, IMMessage msg) {
        // 回复心跳
        IMMessage pong = IMMessage.createHeartbeat();
        ctx.writeAndFlush(pong);
    }

    /**
     * 处理单聊消息
     */
    private void handleSingleChat(ChannelHandlerContext ctx, IMMessage msg) {
        IMMessageDTO dto = new IMMessageDTO();
        dto.setClientMsgId(msg.getClientMsgId());
        dto.setFromUserId(msg.getFromUserId());
        dto.setToUserId(msg.getToUserId());
        dto.setContent(msg.getContent());
        dto.setContentType(msg.getContentType());
        
        messageService.sendSingleMessage(dto);
    }

    /**
     * 处理群聊消息
     */
    private void handleGroupChat(ChannelHandlerContext ctx, IMMessage msg) {
        IMMessageDTO dto = new IMMessageDTO();
        dto.setClientMsgId(msg.getClientMsgId());
        dto.setFromUserId(msg.getFromUserId());
        dto.setToGroupId(msg.getToGroupId());
        dto.setContent(msg.getContent());
        dto.setContentType(msg.getContentType());
        
        messageService.sendGroupMessage(dto);
    }

    /**
     * 处理ACK
     */
    private void handleAck(ChannelHandlerContext ctx, IMMessage msg) {
        messageService.ackMessage(msg.getMsgSeq(), msg.getFromUserId());
    }

    /**
     * 处理已读回执
     */
    private void handleReadReceipt(ChannelHandlerContext ctx, IMMessage msg) {
        messageService.readMessage(msg.getMsgSeq(), msg.getFromUserId());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (sessionManager != null) {
            sessionManager.unbind(ctx.channel());
        }
        log.info("TCP连接断开: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.warn("读超时，关闭连接: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("TCP连接异常: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
