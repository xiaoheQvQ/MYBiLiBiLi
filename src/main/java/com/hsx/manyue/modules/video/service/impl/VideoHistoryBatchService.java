package com.hsx.manyue.modules.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.phantomthief.collection.BufferTrigger;
import com.hsx.manyue.common.config.BufferTriggerConfig;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.modules.video.mapper.VideoHistoryMapper;
import com.hsx.manyue.modules.video.model.entity.VideoHistoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 视频观看历史批量处理服务
 * 优化点：使用快手 BufferTrigger 进行流量聚合，智能批量写入数据库
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoHistoryBatchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final VideoHistoryMapper videoHistoryMapper;
    
    /**
     * 延迟注入 BufferTrigger，解决循环依赖
     */
    @Autowired
    @Lazy
    private BufferTrigger<Map.Entry<BufferTriggerConfig.VideoHistoryKey, BufferTriggerConfig.VideoHistoryData>> videoHistoryBufferTrigger;

    /**
     * 异步更新历史记录（登录用户）
     */
    public void updateHistoryAsync(Long userId, Long videoId, Double time) {
        // 1. 更新 Redis 缓存
        String redisKey = RedisKeys.VIDEO_HISTORY_CACHE + userId + ":" + videoId;
        redisTemplate.opsForValue().set(redisKey, time.toString(), 5, TimeUnit.MINUTES);

        // 2. 构造 Key 和 Data
        BufferTriggerConfig.VideoHistoryKey historyKey =
                new BufferTriggerConfig.VideoHistoryKey(userId, videoId);
        BufferTriggerConfig.VideoHistoryData historyData =
                new BufferTriggerConfig.VideoHistoryData(time);

        // 3. 封装成 Map.Entry 传入 enqueue
        videoHistoryBufferTrigger.enqueue(new AbstractMap.SimpleEntry<>(historyKey, historyData));
    }

    /**
     * 游客观看历史异步更新
     */
    public void updateHistoryUnloginAsync(Long videoId, Double time, String clientId, String ip) {
        // 1. 更新 Redis 缓存
        String key = RedisKeys.VIDEO_HISTORY_CACHE + "guest:" + clientId + ":" + videoId + ":" + ip;
        redisTemplate.opsForValue().set(key, time.toString(), 5, TimeUnit.MINUTES);

        // 2. 构造 Key 和 Data
        BufferTriggerConfig.VideoHistoryKey historyKey =
                new BufferTriggerConfig.VideoHistoryKey(clientId, videoId, ip);
        BufferTriggerConfig.VideoHistoryData historyData =
                new BufferTriggerConfig.VideoHistoryData(time);

        // 3. 封装为单个 Entry 对象传入
        videoHistoryBufferTrigger.enqueue(new AbstractMap.SimpleEntry<>(historyKey, historyData));
    }

    /**
     * 从Redis获取观看进度
     */
    public Double getPlayPosition(Long userId, Long videoId) {
        String key = RedisKeys.VIDEO_HISTORY_CACHE + userId + ":" + videoId;
        String timeStr = redisTemplate.opsForValue().get(key);
        
        if (timeStr != null) {
            return Double.valueOf(timeStr);
        }
        
        // Redis中没有，从数据库查询
        VideoHistoryEntity history = videoHistoryMapper.selectOne(
            new LambdaQueryWrapper<VideoHistoryEntity>()
                .eq(VideoHistoryEntity::getUserId, userId)
                .eq(VideoHistoryEntity::getVideoId, videoId)
        );
        return history == null ? -1 : history.getTime();
    }
    
    /**
     * 手动触发批量刷新
     */
    public void manualFlush() {
        try {
            videoHistoryBufferTrigger.manuallyDoTrigger();
            log.info("手动触发 BufferTrigger 刷新观看历史记录");
        } catch (Exception e) {
            log.error("手动触发 BufferTrigger 刷新失败", e);
        }
    }
}
