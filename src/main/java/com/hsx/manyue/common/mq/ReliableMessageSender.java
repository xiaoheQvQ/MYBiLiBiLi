package com.hsx.manyue.common.mq;

import com.hsx.manyue.common.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MQ消息发送可靠性保证组件
 * 整合Spring Retry重试框架 + 补偿发送方案
 */
@Component
@RequiredArgsConstructor
@Slf4j
@EnableRetry
public class ReliableMessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    
    // 消息补偿队列前缀
    private static final String MESSAGE_COMPENSATION_KEY = "mq:compensation:";
    
    // 消息发送记录（用于确认回调）
    private static final String MESSAGE_CONFIRM_KEY = "mq:confirm:";

    /**
     * 配置RabbitMQ发送确认回调
     */
    @PostConstruct
    public void setupConfirmCallback() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            
            String messageId = correlationData.getId();
            if (ack) {
                // 消息成功发送到Exchange，移除补偿记录
                redisTemplate.delete(MESSAGE_COMPENSATION_KEY + messageId);
                redisTemplate.delete(MESSAGE_CONFIRM_KEY + messageId);
                // 【优化】从待处理集合中移除
                redisTemplate.opsForSet().remove(RedisKeys.MQ_COMPENSATION_PENDING_KEYS, messageId);
                log.debug("消息发送成功，messageId: {}", messageId);
            } else {
                // 消息发送失败，记录到补偿队列
                log.error("消息发送失败，messageId: {}, 原因: {}", messageId, cause);
                // 补偿记录已在发送时创建，这里不需要额外处理
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            // 消息从Exchange路由到Queue失败时回调
            log.error("消息路由失败: {}, 交换机: {}, 路由键: {}, 原因: {}", 
                    returned.getMessage(), 
                    returned.getExchange(), 
                    returned.getRoutingKey(), 
                    returned.getReplyText());
        });
    }

    /**
     * 可靠发送消息（带重试机制）
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息内容
     */
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendMessageReliably(String exchange, String routingKey, String message) {
        String messageId = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(messageId);
        
        try {
            // 1. 先将消息存入Redis补偿队列（24小时过期）
            String compensationKey = MESSAGE_COMPENSATION_KEY + messageId;
            MessageCompensationInfo info = new MessageCompensationInfo(exchange, routingKey, message);
            redisTemplate.opsForValue().set(compensationKey, info.toJson(), 24, TimeUnit.HOURS);
            
            // 2. 将 messageId 添加到待处理集合（【优化】避免 keys() 扫描）
            redisTemplate.opsForSet().add(RedisKeys.MQ_COMPENSATION_PENDING_KEYS, messageId);
            
            // 3. 发送消息
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
            
            log.info("消息发送成功，messageId: {}, exchange: {}, routingKey: {}", 
                    messageId, exchange, routingKey);
        } catch (Exception e) {
            log.error("消息发送异常，messageId: {}, 将进行重试", messageId, e);
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 定时补偿任务：每5分钟检查失败的消息并重新发送
     */
    @Scheduled(fixedDelay = 300000) // 5分钟
    public void compensateFailedMessages() {
        try {
            // 【优化】使用 Set 维护待补偿的 messageId，避免 keys() 全量扫描
            Set<String> pendingMessageIds = redisTemplate.opsForSet().members(RedisKeys.MQ_COMPENSATION_PENDING_KEYS);            if (pendingMessageIds == null || pendingMessageIds.isEmpty()) {
                return;
            }

            int compensateCount = 0;
            for (Object messageIdObj : pendingMessageIds) {
                String messageId = messageIdObj.toString();
                String key = MESSAGE_COMPENSATION_KEY + messageId;
                String messageJson = redisTemplate.opsForValue().get(key);
                if (messageJson == null) {
                    // 消息已被删除，从待处理集合中移除
                    redisTemplate.opsForSet().remove(RedisKeys.MQ_COMPENSATION_PENDING_KEYS, messageIdObj);
                    continue;
                }

                MessageCompensationInfo info = MessageCompensationInfo.fromJson(messageJson);
                
                // 检查消息是否已发送超过5分钟（说明可能发送失败）
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl < 24 * 3600 - 300) { // 超过5分钟
                    try {
                        // 重新发送消息
                        String newMessageId = UUID.randomUUID().toString();
                        CorrelationData correlationData = new CorrelationData(newMessageId);
                        rabbitTemplate.convertAndSend(info.getExchange(), info.getRoutingKey(), 
                                info.getMessage(), correlationData);
                        
                        // 删除旧的补偿记录，创建新的
                        redisTemplate.delete(key);
                        redisTemplate.opsForSet().remove(RedisKeys.MQ_COMPENSATION_PENDING_KEYS, messageIdObj);
                        
                        String newKey = MESSAGE_COMPENSATION_KEY + newMessageId;
                        redisTemplate.opsForValue().set(newKey, messageJson, 24, TimeUnit.HOURS);
                        redisTemplate.opsForSet().add(RedisKeys.MQ_COMPENSATION_PENDING_KEYS, newMessageId);
                        
                        compensateCount++;
                        log.info("补偿发送消息成功，newMessageId: {}, exchange: {}", 
                                newMessageId, info.getExchange());
                    } catch (Exception e) {
                        log.error("补偿发送消息失败: {}", info, e);
                    }
                }
            }

            if (compensateCount > 0) {
                log.info("消息补偿任务完成，共补偿 {} 条消息", compensateCount);
            }
        } catch (Exception e) {
            log.error("消息补偿任务执行失败", e);
        }
    }

    /**
     * 消息补偿信息
     */
    private static class MessageCompensationInfo {
        private String exchange;
        private String routingKey;
        private String message;

        public MessageCompensationInfo(String exchange, String routingKey, String message) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
        }

        public String getExchange() {
            return exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public String getMessage() {
            return message;
        }

        public String toJson() {
            return String.format("{\"exchange\":\"%s\",\"routingKey\":\"%s\",\"message\":\"%s\"}", 
                    exchange, routingKey, message.replace("\"", "\\\""));
        }

        public static MessageCompensationInfo fromJson(String json) {
            // 简单的JSON解析（生产环境建议使用Jackson或Gson）
            String exchange = json.substring(json.indexOf("\"exchange\":\"") + 12);
            exchange = exchange.substring(0, exchange.indexOf("\""));
            
            String routingKey = json.substring(json.indexOf("\"routingKey\":\"") + 14);
            routingKey = routingKey.substring(0, routingKey.indexOf("\""));
            
            String message = json.substring(json.indexOf("\"message\":\"") + 11);
            message = message.substring(0, message.lastIndexOf("\""));
            
            return new MessageCompensationInfo(exchange, routingKey, message);
        }

        @Override
        public String toString() {
            return toJson();
        }
    }
}
