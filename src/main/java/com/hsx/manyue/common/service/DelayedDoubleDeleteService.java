package com.hsx.manyue.common.service;

import com.hsx.manyue.common.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 延迟双删服务 - 保证 Redis 缓存与数据库一致性
 * 
 * 策略：
 * 1. 更新数据前删除缓存
 * 2. 更新数据库
 * 3. 延迟 500ms 后再次删除缓存
 * 4. 失败重试（最多3次）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DelayedDoubleDeleteService {

    private final RedisTemplate<String, String> redisTemplate;
    private final CacheService cacheService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final long DELAY_MS = 500; // 延迟时间
    private static final int MAX_RETRIES = 3; // 最大重试次数

    /**
     * 第一次删除缓存（更新数据前）
     * 
     * @param key 缓存键
     */
    public void firstDelete(String key) {
        try {
            cacheService.delete(key);
            log.info("第一次删除缓存成功: {}", key);
        } catch (Exception e) {
            log.error("第一次删除缓存失败: {}", key, e);
            // 第一次删除失败不影响流程，继续执行
        }
    }

    /**
     * 第二次删除缓存（延迟执行）
     * 
     * @param key 缓存键
     */
    public void secondDelete(String key) {
        scheduler.schedule(() -> {
            deleteWithRetry(key, 0);
        }, DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 带重试的删除缓存
     */
    private void deleteWithRetry(String key, int retryCount) {
        try {
            cacheService.delete(key);
            log.info("第二次删除缓存成功: key={}, retryCount={}", key, retryCount);
        } catch (Exception e) {
            if (retryCount < MAX_RETRIES) {
                log.warn("第二次删除缓存失败，准备重试: key={}, retryCount={}", key, retryCount, e);
                // 指数退避重试
                long delayMs = (long) Math.pow(2, retryCount) * 100;
                scheduler.schedule(() -> {
                    deleteWithRetry(key, retryCount + 1);
                }, delayMs, TimeUnit.MILLISECONDS);
            } else {
                log.error("第二次删除缓存最终失败: key={}, retryCount={}", key, retryCount, e);
                // 记录到待补偿队列
                recordFailure(key);
            }
        }
    }

    /**
     * 记录删除失败的key，用于后续补偿
     */
    private void recordFailure(String key) {
        try {
            redisTemplate.opsForSet().add("cache:delete:failed", key);
            log.warn("记录删除失败的缓存key: {}", key);
        } catch (Exception e) {
            log.error("记录删除失败key时出错: {}", key, e);
        }
    }

    /**
     * 完整的双删流程
     * 
     * @param key 缓存键
     * @param updateAction 数据库更新操作
     */
    public void doubleDelete(String key, Runnable updateAction) {
        // 1. 第一次删除缓存
        firstDelete(key);

        try {
            // 2. 执行数据库更新
            updateAction.run();
            log.info("数据库更新成功: {}", key);
        } catch (Exception e) {
            log.error("数据库更新失败: {}", key, e);
            throw e; // 更新失败则抛出异常
        }

        // 3. 延迟第二次删除
        secondDelete(key);
    }

    /**
     * 批量双删（用于批量更新场景）
     * 
     * @param keys 缓存键列表
     * @param updateAction 数据库批量更新操作
     */
    public void batchDoubleDelete(Iterable<String> keys, Runnable updateAction) {
        // 1. 批量第一次删除
        for (String key : keys) {
            firstDelete(key);
        }

        try {
            // 2. 执行批量更新
            updateAction.run();
            log.info("批量数据库更新成功");
        } catch (Exception e) {
            log.error("批量数据库更新失败", e);
            throw e;
        }

        // 3. 批量延迟第二次删除
        for (String key : keys) {
            secondDelete(key);
        }
    }

    /**
     * 补偿处理（定时任务，处理失败的删除操作）
     */
    public void compensate() {
        try {
            // 获取所有失败的key
            var failedKeys = redisTemplate.opsForSet().members("cache:delete:failed");
            if (failedKeys == null || failedKeys.isEmpty()) {
                return;
            }

            log.info("开始补偿删除失败的缓存，共{}个", failedKeys.size());
            
            for (String key : failedKeys) {
                try {
                    cacheService.delete(key);
                    // 删除成功后移除记录
                    redisTemplate.opsForSet().remove("cache:delete:failed", key);
                    log.info("补偿删除成功: {}", key);
                } catch (Exception e) {
                    log.error("补偿删除失败: {}", key, e);
                }
            }
        } catch (Exception e) {
            log.error("补偿处理异常", e);
        }
    }
}
