package com.hsx.manyue.modules.chat.mq;

import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.config.MQConfig;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.chat.service.IChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 私信 MQ 消费者
 * 批量处理私信消息，提升数据库写入效率
 * 
 * 优化点：
 * 1. 支持批量消息接收（每批最多50条）
 * 2. 使用 MyBatis saveBatch 批量插入
 * 3. 异步处理，不阻塞用户请求
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageMQConsumer {

    private final IChatMessageService chatMessageService;

    /**
     * 消费私信消息（支持批量）
     * 
     * @param messages 消息列表
     */
    @RabbitListener(queues = MQConfig.PRIVATE_MESSAGE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receivePrivateMessages(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        log.info("开始批量处理私信消息，共 {} 条", messages.size());
        
        List<ChatMessage> chatMessages = new ArrayList<>();
        
        for (String message : messages) {
            try {
                ChatMessage chatMessage = JSONUtil.toBean(message, ChatMessage.class);
                chatMessages.add(chatMessage);
            } catch (Exception e) {
                log.error("解析私信消息失败: {}", message, e);
            }
        }

        // 批量保存私信
        if (!chatMessages.isEmpty()) {
            try {
                chatMessageService.saveBatch(chatMessages);
                log.info("批量保存私信成功，共 {} 条", chatMessages.size());
            } catch (Exception e) {
                log.error("批量保存私信失败", e);
            }
        }
    }
}
