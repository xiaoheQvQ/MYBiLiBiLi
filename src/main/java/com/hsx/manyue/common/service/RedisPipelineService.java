package com.hsx.manyue.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis Pipeline 服务 - 批量操作优化
 * 
 * 优势：
 * 1. 减少网络 RTT（Round Trip Time）
 * 2. 批量操作性能提升 10倍+
 * 3. 适用于批量写入、批量读取场景
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPipelineService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 批量 increment（用于视频计数）
     * 
     * @param increments key -> increment 值的映射
     */
    public void batchIncrement(Map<String, Long> increments) {
        if (increments == null || increments.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            increments.forEach((key, value) -> {
                connection.incrBy(key.getBytes(), value);
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量increment完成: {} 个操作，耗时 {}ms", increments.size(), elapsed);
    }

    /**
     * 批量 set（用于缓存预热）
     * 
     * @param data key -> value 映射
     * @param ttlSeconds 过期时间（秒）
     */
    public void batchSet(Map<String, String> data, long ttlSeconds) {
        if (data == null || data.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            data.forEach((key, value) -> {
                connection.setEx(key.getBytes(), ttlSeconds, value.getBytes());
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量set完成: {} 个操作，耗时 {}ms", data.size(), elapsed);
    }

    /**
     * 批量 delete（用于缓存清理）
     * 
     * @param keys 要删除的key列表
     */
    public void batchDelete(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            keys.forEach(key -> {
                connection.del(key.getBytes());
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量delete完成: {} 个操作，耗时 {}ms", keys.size(), elapsed);
    }

    /**
     * 批量 sadd（用于集合添加）
     * 
     * @param setKey 集合key
     * @param members 成员列表
     */
    public void batchSAdd(String setKey, List<String> members) {
        if (members == null || members.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] keyBytes = setKey.getBytes();
            members.forEach(member -> {
                connection.sAdd(keyBytes, member.getBytes());
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量sadd完成: {} 个操作，耗时 {}ms", members.size(), elapsed);
    }

    /**
     * 批量 expire（用于批量设置过期时间）
     * 
     * @param keys key列表
     * @param ttlSeconds 过期时间（秒）
     */
    public void batchExpire(List<String> keys, long ttlSeconds) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            keys.forEach(key -> {
                connection.expire(key.getBytes(), ttlSeconds);
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量expire完成: {} 个操作，耗时 {}ms", keys.size(), elapsed);
    }

    /**
     * 批量 hset（用于hash批量写入）
     * 
     * @param hashKey hash key
     * @param data field -> value 映射
     */
    public void batchHSet(String hashKey, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] keyBytes = hashKey.getBytes();
            data.forEach((field, value) -> {
                connection.hSet(keyBytes, field.getBytes(), value.getBytes());
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量hset完成: {} 个操作，耗时 {}ms", data.size(), elapsed);
    }

    /**
     * 批量获取（用于批量读取）
     * 
     * @param keys key列表
     * @return 值列表
     */
    public List<Object> batchGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        long startTime = System.currentTimeMillis();
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            keys.forEach(key -> {
                connection.get(key.getBytes());
            });
            return null;
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Pipeline批量get完成: {} 个操作，耗时 {}ms", keys.size(), elapsed);
        
        return results;
    }
}
