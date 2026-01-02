package com.hsx.manyue.common.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;


@Configuration
public class MQConfig {

    // 弹幕相关配置
    public static final String DANAMKU_EXCHANGE = "danmaku-exchange";
    public static final String DANAMKU_QUEUE = "danmaku-queue";


    // 视频处理相关配置
    public static final String VIDEO_PROCESS_EXCHANGE = "video-process-exchange";
    public static final String VIDEO_PROCESS_QUEUE = "video-process-queue";

    // 【新增】评论相关配置
    public static final String COMMENT_EXCHANGE = "comment-exchange";
    public static final String COMMENT_QUEUE = "comment-queue";
    public static final String COMMENT_ROUTING_KEY = "comment.publish";

    // 【新增】私信相关配置
    public static final String PRIVATE_MESSAGE_EXCHANGE = "message-exchange";
    public static final String PRIVATE_MESSAGE_QUEUE = "message-queue";
    public static final String PRIVATE_MESSAGE_ROUTING_KEY = "message.send";


    @Bean
    public Queue danmakuQueue() {
        return new Queue(DANAMKU_QUEUE);
    }

    @Bean
    public FanoutExchange danmakuExchange() {
        return new FanoutExchange(DANAMKU_EXCHANGE);
    }

    @Bean
    public Binding danmakuBinding() {
        return BindingBuilder.bind(danmakuQueue()).to(danmakuExchange());
    }


    // 新增视频处理队列配置
    @Bean
    public Queue videoProcessQueue() {
        return new Queue(VIDEO_PROCESS_QUEUE);
    }

    @Bean
    public FanoutExchange videoProcessExchange() {
        return new FanoutExchange(VIDEO_PROCESS_EXCHANGE);
    }

    @Bean
    public Binding videoProcessBinding() {
        return BindingBuilder.bind(videoProcessQueue()).to(videoProcessExchange());
    }

    // 【新增】评论队列配置
    @Bean
    public Queue commentQueue() {
        return new Queue(COMMENT_QUEUE, true); // 持久化队列
    }

    @Bean
    public FanoutExchange commentExchange() {
        return new FanoutExchange(COMMENT_EXCHANGE, true, false);
    }

    @Bean
    public Binding commentBinding() {
        return BindingBuilder.bind(commentQueue()).to(commentExchange());
    }

    // 【新增】私信队列配置
    @Bean
    public Queue privateMessageQueue() {
        return new Queue(PRIVATE_MESSAGE_QUEUE, true); // 持久化队列
    }

    @Bean
    public FanoutExchange privateMessageExchange() {
        return new FanoutExchange(PRIVATE_MESSAGE_EXCHANGE, true, false);
    }

    @Bean
    public Binding privateMessageBinding() {
        return BindingBuilder.bind(privateMessageQueue()).to(privateMessageExchange());
    }

    /**
     * 配置 RabbitMQ 监听器容器工厂
     * 修复批量消息接收时的解析错误
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // 设置批量消费
        factory.setBatchListener(true);
        factory.setBatchSize(50); // 每批最多50条消息
        factory.setConsumerBatchEnabled(true);
        
        // 设置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        
        // 设置预取数量
        factory.setPrefetchCount(50);
        
        return factory;
    }

}
