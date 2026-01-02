package com.hsx.manyue.modules.im.server;

import cn.hutool.json.JSONUtil;
import com.hsx.manyue.modules.im.model.IMMessage;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 会话管理器 - 管理用户连接和在线状态
 * 支持多端登录
 */
@Slf4j
@Component
public class SessionManager {

    /**
     * userId -> Channel列表映射 (支持多端登录)
     */
    private final Map<Long, List<Channel>> userChannels = new ConcurrentHashMap<>();
    
    /**
     * channelId -> userId映射
     */
    private final Map<String, Long> channelUsers = new ConcurrentHashMap<>();

    /**
     * 绑定用户和Channel
     */
    public void bind(Long userId, Channel channel) {
        List<Channel> channels = userChannels.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        channels.add(channel);
        channelUsers.put(channel.id().asLongText(), userId);
        log.info("用户 {} 绑定连接: {}, 当前在线设备数: {}", userId, channel.id().asShortText(), channels.size());
    }

    /**
     * 解绑
     */
    public void unbind(Channel channel) {
        Long userId = channelUsers.remove(channel.id().asLongText());
        if (userId != null) {
            List<Channel> channels = userChannels.get(userId);
            if (channels != null) {
                channels.remove(channel);
                if (channels.isEmpty()) {
                    userChannels.remove(userId);
                    log.info("用户 {} 所有连接已断开", userId);
                } else {
                    log.info("用户 {} 断开一个连接，剩余设备数: {}", userId, channels.size());
                }
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        List<Channel> channels = userChannels.get(userId);
        return channels != null && !channels.isEmpty();
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(Channel channel) {
        return channelUsers.get(channel.id().asLongText());
    }

    /**
     * 推送消息给指定用户（所有在线设备）
     */
    public void pushMessage(Long userId, IMMessage message) {
        List<Channel> channels = userChannels.get(userId);
        if (channels != null && !channels.isEmpty()) {
            String messageJson = message.toJson();
            for (Channel channel : channels) {
                if (channel.isActive()) {
                    try {
                        // 判断是WebSocket还是TCP
                        if (channel.pipeline().get("websocket-handler") != null) {
                            // WebSocket
                            channel.writeAndFlush(new TextWebSocketFrame(messageJson));
                        } else {
                            // TCP
                            channel.writeAndFlush(message);
                        }
                        log.debug("推送消息给用户 {}: {}", userId, messageJson);
                    } catch (Exception e) {
                        log.error("推送消息失败: userId={}, channel={}", userId, channel.id().asShortText(), e);
                    }
                }
            }
        } else {
            log.info("用户 {} 不在线, 消息将存入离线队列", userId);
        }
    }

    /**
     * 推送消息实体给指定用户
     */
    public void pushMessageEntity(Long userId, IMMessageEntity entity) {
        IMMessage message = convertToIMMessage(entity);
        pushMessage(userId, message);
    }

    /**
     * 推送群消息
     */
    public void pushGroupMessage(Long groupId, IMMessage message, List<Long> memberIds) {
        log.info("推送群消息: groupId={}, 成员数={}", groupId, memberIds.size());
        for (Long memberId : memberIds) {
            pushMessage(memberId, message);
        }
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return userChannels.size();
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnectionCount() {
        return channelUsers.size();
    }

    /**
     * 转换消息实体为IM消息
     */
    private IMMessage convertToIMMessage(IMMessageEntity entity) {
        IMMessage message = new IMMessage();
        message.setMsgType(entity.getMsgType().byteValue());
        message.setMsgSeq(entity.getMsgSeq());
        message.setFromUserId(entity.getFromUserId());
        message.setToUserId(entity.getToUserId());
        message.setToGroupId(entity.getToGroupId());
        message.setContent(entity.getContent());
        message.setContentType(entity.getContentType());
        message.setMsgTime(entity.getMsgTime());
        message.setClientMsgId(entity.getClientMsgId());
        return message;
    }
}
