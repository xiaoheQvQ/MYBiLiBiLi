package com.hsx.manyue.common.service;

import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务 - 统一实现缓存策略
 * 
 * 防护策略：
 * 1. 缓存雪崩防护：过期时间随机化 + 多级缓存（Caffeine + Redis）
 * 2. 缓存穿透防护：布隆过滤器 + 空值缓存
 * 3. 缓存击穿防护：分布式锁 + 热点数据永不过期
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    // 本地缓存（Caffeine）- 一级缓存
    private Cache<String, Object> localCache;

    // 布隆过滤器（防止缓存穿透）
    private RBloomFilter<String> bloomFilter;

    @PostConstruct
    public void init() {
        // 初始化本地缓存（最多10000个条目，5分钟过期）
        localCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build();

        // 初始化布隆过滤器
        bloomFilter = redissonClient.getBloomFilter("cache:bloom-filter");
        if (!bloomFilter.isExists()) {
            // 预期100万条数据，误判率0.01
            bloomFilter.tryInit(1000000L, 0.01);
        }

        log.info("缓存服务初始化完成：本地缓存 + 布隆过滤器");
    }

    /**
     * 获取缓存数据（多级缓存 + 全套防护策略）
     * 
     * @param key 缓存键
     * @param loader 数据加载器（缓存未命中时调用）
     * @param ttl 过期时间（秒），会添加随机偏移防止雪崩
     * @param isHotKey 是否热点数据（如果是，使用分布式锁防止击穿）
     * @return 缓存数据
     */
    public <T> T get(String key, Supplier<T> loader, long ttl, boolean isHotKey) {
        // 1. 先查本地缓存（一级缓存）
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            log.debug("本地缓存命中: {}", key);
            return (T) localValue;
        }

        // 2. 查 Redis 缓存（二级缓存）
        Object redisValue = redisTemplate.opsForValue().get(key);
        if (redisValue != null) {
            log.debug("Redis缓存命中: {}", key);
            // 回写到本地缓存
            localCache.put(key, redisValue);
            
            // 如果是空值缓存，返回null
            if ("NULL".equals(redisValue)) {
                return null;
            }
            return (T) redisValue;
        }

        // 3. 缓存未命中，检查布隆过滤器（防穿透）
        if (!bloomFilter.contains(key)) {
            log.warn("布隆过滤器拦截非法key: {}", key);
            // 缓存空值，防止频繁查询数据库
            cacheNullValue(key);
            return null;
        }

        // 4. 从数据库加载数据
        T value;
        if (isHotKey) {
            // 热点数据：使用分布式锁防止击穿
            value = loadWithLock(key, loader, ttl);
        } else {
            // 普通数据：直接加载
            value = loadData(key, loader, ttl);
        }

        return value;
    }

    /**
     * 使用分布式锁加载数据（防止缓存击穿）
     */
    private <T> T loadWithLock(String key, Supplier<T> loader, long ttl) {
        RLock lock = redissonClient.getLock("lock:cache:" + key);
        try {
            // 尝试获取锁，最多等待100ms
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // 双重检查：获取锁后再次查询缓存
                    Object cachedValue = redisTemplate.opsForValue().get(key);
                    if (cachedValue != null) {
                        if ("NULL".equals(cachedValue)) {
                            return null;
                        }
                        return (T) cachedValue;
                    }

                    // 真正加载数据
                    return loadData(key, loader, ttl);
                } finally {
                    lock.unlock();
                }
            } else {
                // 获取锁失败，等待一下再查缓存
                Thread.sleep(50);
                Object cachedValue = redisTemplate.opsForValue().get(key);
                if (cachedValue != null && !"NULL".equals(cachedValue)) {
                    return (T) cachedValue;
                }
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", key, e);
            return null;
        }
    }

    /**
     * 加载数据并写入多级缓存
     */
    private <T> T loadData(String key, Supplier<T> loader, long ttl) {
        T value = loader.get();

        if (value == null) {
            // 空值缓存（防穿透）
            cacheNullValue(key);
            return null;
        }

        // 写入 Redis（添加随机过期时间，防雪崩）
        long randomTtl = ttl + RandomUtil.randomLong(0, (long) (ttl * 0.2));
        redisTemplate.opsForValue().set(key, value, randomTtl, TimeUnit.SECONDS);

        // 写入本地缓存
        localCache.put(key, value);

        // 添加到布隆过滤器
        bloomFilter.add(key);

        log.info("加载数据并写入缓存: key={}, ttl={}s", key, randomTtl);
        return value;
    }

    /**
     * 缓存空值（防穿透）
     */
    private void cacheNullValue(String key) {
        redisTemplate.opsForValue().set(key, "NULL", 5, TimeUnit.MINUTES);
        localCache.put(key, "NULL");
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
        log.info("删除缓存: {}", key);
    }

    /**
     * 预热缓存（批量添加到布隆过滤器）
     */
    public void warmUp(Iterable<String> keys) {
        for (String key : keys) {
            bloomFilter.add(key);
        }
        log.info("布隆过滤器预热完成");
    }
}
