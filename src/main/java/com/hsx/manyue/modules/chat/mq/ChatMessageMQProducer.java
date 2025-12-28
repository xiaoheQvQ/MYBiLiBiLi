package com.hsx.manyue.modules.chat.mq;

import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.config.MQConfig;
import com.hsx.manyue.common.mq.ReliableMessageSender;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 私信 MQ 生产者
 * 将私信发送操作异步化，通过 MQ 进行流量削峰
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageMQProducer {

    private final ReliableMessageSender reliableMessageSender;

    /**
     * 发送私信消息
     * 
     * @param chatMessage 私信对象
     */
    public void sendPrivateMessage(ChatMessage chatMessage) {
        try {
            String message = JSONUtil.toJsonStr(chatMessage);
            reliableMessageSender.sendMessageReliably(
                    MQConfig.PRIVATE_MESSAGE_EXCHANGE,
                    MQConfig.PRIVATE_MESSAGE_ROUTING_KEY,
                    message
            );
            log.info("私信消息发送成功，fromUserId: {}, toUserId: {}", 
                    chatMessage.getFromUserId(), chatMessage.getToUserId());
        } catch (Exception e) {
            log.error("私信消息发送失败", e);
            throw new RuntimeException("私信发送失败，请稍后重试");
        }
    }
}
