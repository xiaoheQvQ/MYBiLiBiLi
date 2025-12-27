package com.hsx.manyue.common.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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


}
