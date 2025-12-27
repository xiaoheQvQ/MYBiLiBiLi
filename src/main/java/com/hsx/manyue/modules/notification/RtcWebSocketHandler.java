package com.hsx.manyue.modules.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class RtcWebSocketHandler extends TextWebSocketHandler {

    private final UserManager userManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserIdFromQuery(session.getUri().getQuery());

        if (userId == null || userId.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            log.warn("拒绝连接: 未提供用户ID");
            return;
        }

        // 将用户保存
        userManager.addUser(userId, session);
        // 广播join消息
        userManager.sendMessageAllUser();
        log.info("用户 {} 加入, 当前用户数: {}", userId, userManager.getAllUserId().size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到消息: {}", message.getPayload());
        // 可以在这里添加自定义消息处理逻辑
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = userManager.removeUserBySession(session);
        if (userId != null) {
            log.info("用户 {} 退出, 原因: {}", userId, status);
            // 广播leave消息
            userManager.broadcastLeave(userId);
        }
    }

    private String extractUserIdFromQuery(String query) {
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}