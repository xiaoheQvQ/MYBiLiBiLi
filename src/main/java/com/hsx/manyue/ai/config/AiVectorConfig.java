package com.hsx.manyue.ai.config;


import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPooled;

@Configuration
public class AiVectorConfig {


    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;


    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(host, port);
    }


    /**
     * 手动注入 VectorStore
     * @param embeddingModel 必须确保 Spring AI 的模型 Starter 已正确加载
     */
    @Bean
    public RedisVectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("manyue_index")
                .prefix("doc:")
                .initializeSchema(true)
                .build();
    }
}