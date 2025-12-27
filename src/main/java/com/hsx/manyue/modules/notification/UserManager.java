package com.hsx.manyue.modules.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component
public class UserManager {
    private final Map<String, WebSocketSession> userMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, BlockingQueue<String>> messageQueues = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    public void addUser(String userId, WebSocketSession session) {
        userMap.put(userId, session);
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        messageQueues.put(userId, queue);

        // 启动消息发送线程
        executorService.execute(() -> {
            try {
                while (session.isOpen()) {
                    String message = queue.take();
                    synchronized (session) {  // 对session加锁确保顺序发送
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("消息发送失败", e);
            } finally {
                messageQueues.remove(userId);
            }
        });
    }

    public String removeUserBySession(WebSocketSession session) {
        Optional<Map.Entry<String, WebSocketSession>> entry = userMap.entrySet().stream()
                .filter(e -> e.getValue().equals(session))
                .findFirst();

        if (entry.isPresent()) {
            userMap.remove(entry.get().getKey());
            return entry.get().getKey();
        }
        return null;
    }

    public void removeUser(String userId) {
        userMap.remove(userId);
    }

    public WebSocketSession getUser(String userId) {
        return userMap.get(userId);
    }

    public List<String> getAllUserId() {
        return new ArrayList<>(userMap.keySet());
    }

    public void sendMessageAllUser() throws IOException {
        List<String> allUserId = getAllUserId();
        MessageOut messageOut = new MessageOut("join", allUserId);

        for (Map.Entry<String, WebSocketSession> entry : userMap.entrySet()) {
            try {
                String messageText = objectMapper.writeValueAsString(messageOut);
                entry.getValue().sendMessage(new TextMessage(messageText));
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败", entry.getKey(), e);
            }
        }
    }

    public void broadcastLeave(String leftUserId) {
        MessageOut messageOut = new MessageOut("leave", leftUserId);

        for (Map.Entry<String, WebSocketSession> entry : userMap.entrySet()) {
            if (!entry.getKey().equals(leftUserId)) {
                try {
                    String messageText = objectMapper.writeValueAsString(messageOut);
                    entry.getValue().sendMessage(new TextMessage(messageText));
                } catch (IOException e) {
                    log.error("广播离开消息给用户 {} 失败", entry.getKey(), e);
                }
            }
        }
    }


    public void sendMessage(MessageReceive messageReceive) {
        BlockingQueue<String> queue = messageQueues.get(messageReceive.getUserId());
        if (queue != null) {
            try {
                MessageOut messageOut = new MessageOut(messageReceive.getType(), messageReceive.getData());
                String messageText = objectMapper.writeValueAsString(messageOut);
                queue.put(messageText);
            } catch (JsonProcessingException e) {
                log.error("消息序列化失败", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            log.warn("用户 {} 的消息队列不存在", messageReceive.getUserId());
        }
    }

}