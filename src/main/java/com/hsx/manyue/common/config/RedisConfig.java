package com.hsx.manyue.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // 导入@Primary注解
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 方案1：String, Object 类型的RedisTemplate（推荐作为默认Bean，添加@Primary）
    @Bean
    @Primary // 在这里添加@Primary，指定该Bean为同类型的默认优先注入实例
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return createRedisTemplate(redisConnectionFactory);
    }

    // 方案2：Object, Object 类型的RedisTemplate（非默认，不标注@Primary）
    @Bean
    public RedisTemplate<Object, Object> objectRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        configureRedisTemplate(template, redisConnectionFactory);
        return template;
    }

    private <K, V> void configureRedisTemplate(RedisTemplate<K, V> template, RedisConnectionFactory redisConnectionFactory) {
        template.setConnectionFactory(redisConnectionFactory);

        // 使用String序列化器处理Key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 使用GenericJackson2JsonRedisSerializer处理Value（推荐方式）
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        configureRedisTemplate(template, redisConnectionFactory);
        return template;
    }

    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        // 创建ObjectMapper并配置
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()  // 自动注册模块
                .build();

        // 配置objectMapper（如果需要特殊配置）
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}