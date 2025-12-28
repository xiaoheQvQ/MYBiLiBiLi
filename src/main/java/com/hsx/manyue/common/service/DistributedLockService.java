package com.hsx.manyue.common.service;

import com.hsx.manyue.common.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务
 * 用于保证高并发场景下的数据一致性
 * 
 * 特性：
 * 1. 防止死锁：使用UUID标识锁持有者，只有持有者能释放锁
 * 2. 自动过期：设置锁过期时间，防止永久锁定
 * 3. Lua脚本：保证释放锁操作的原子性
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final RedisTemplate<String, String> redisTemplate;

    // Lua脚本：原子性释放锁（检查+删除）
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "   return redis.call('del', KEYS[1]) " +
        "else " +
        "   return 0 " +
        "end";

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的键
     * @param expireSeconds 锁过期时间（秒）
     * @return 锁标识（用于释放锁），获取失败返回null
     */
    public String tryLock(String lockKey, long expireSeconds) {
        String lockValue = UUID.randomUUID().toString();
        String key = RedisKeys.DISTRIBUTED_LOCK + lockKey;
        
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, lockValue, expireSeconds, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功: {}", lockKey);
            return lockValue;
        }
        
        log.debug("获取分布式锁失败: {}", lockKey);
        return null;
    }

    /**
     * 尝试获取锁（带自旋等待）
     * @param lockKey 锁的键
     * @param expireSeconds 锁过期时间（秒）
     * @param waitTimeMs 最大等待时间（毫秒）
     * @param retryIntervalMs 重试间隔（毫秒）
     * @return 锁标识，获取失败返回null
     */
    public String tryLockWithRetry(String lockKey, long expireSeconds, 
                                    long waitTimeMs, long retryIntervalMs) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < waitTimeMs) {
            String lockValue = tryLock(lockKey, expireSeconds);
            if (lockValue != null) {
                return lockValue;
            }
            
            try {
                Thread.sleep(retryIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁等待被中断: {}", lockKey, e);
                return null;
            }
        }
        
        log.warn("获取分布式锁超时: {}, 等待时间: {}ms", lockKey, waitTimeMs);
        return null;
    }

    /**
     * 释放锁（使用Lua脚本保证原子性）
     * @param lockKey 锁的键
     * @param lockValue 锁标识
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }
        
        String key = RedisKeys.DISTRIBUTED_LOCK + lockKey;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        
        Long result = redisTemplate.execute(script, 
                Collections.singletonList(key), 
                lockValue);
        
        boolean success = result != null && result == 1;
        if (success) {
            log.debug("释放分布式锁成功: {}", lockKey);
        } else {
            log.warn("释放分布式锁失败（锁已过期或不是持有者）: {}", lockKey);
        }
        
        return success;
    }

    /**
     * 使用分布式锁执行业务逻辑（自动加锁和释放）
     * @param lockKey 锁的键
     * @param expireSeconds 锁过期时间（秒）
     * @param supplier 业务逻辑
     * @return 业务逻辑返回值
     * @throws IllegalStateException 获取锁失败
     */
    public <T> T executeWithLock(String lockKey, long expireSeconds, Supplier<T> supplier) {
        String lockValue = tryLock(lockKey, expireSeconds);
        if (lockValue == null) {
            throw new IllegalStateException("获取分布式锁失败: " + lockKey);
        }
        
        try {
            return supplier.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 使用分布式锁执行业务逻辑（带重试）
     * @param lockKey 锁的键
     * @param expireSeconds 锁过期时间（秒）
     * @param waitTimeMs 最大等待时间（毫秒）
     * @param retryIntervalMs 重试间隔（毫秒）
     * @param supplier 业务逻辑
     * @return 业务逻辑返回值
     * @throws IllegalStateException 获取锁失败
     */
    public <T> T executeWithLockRetry(String lockKey, long expireSeconds, 
                                       long waitTimeMs, long retryIntervalMs,
                                       Supplier<T> supplier) {
        String lockValue = tryLockWithRetry(lockKey, expireSeconds, waitTimeMs, retryIntervalMs);
        if (lockValue == null) {
            throw new IllegalStateException("获取分布式锁超时: " + lockKey);
        }
        
        try {
            return supplier.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 检查锁是否存在
     * @param lockKey 锁的键
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        String key = RedisKeys.DISTRIBUTED_LOCK + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
