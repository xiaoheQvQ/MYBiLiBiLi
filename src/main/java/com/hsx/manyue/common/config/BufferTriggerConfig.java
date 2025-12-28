package com.hsx.manyue.common.config;

import com.github.phantomthief.collection.BufferTrigger;
import com.github.phantomthief.collection.impl.SimpleBufferTrigger;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.modules.video.service.IVideoService;
import com.hsx.manyue.modules.video.service.impl.VideoHistoryBatchProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BufferTrigger 配置类
 * 用于流量聚合和批量处理，减少数据库和 Redis 的访问频率
 * 
 * 重构说明：
 * - 移除了对 IVideoHistoryService 的直接依赖，改为依赖 VideoHistoryBatchProcessor
 * - 解决了循环依赖问题：BufferTriggerConfig -> IVideoHistoryService -> VideoHistoryBatchService -> BufferTrigger
 * - 应用了依赖倒置原则 (DIP) 和单一职责原则 (SRP)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BufferTriggerConfig {

    private final RedisTemplate<String, String> redisTemplate;
    private final VideoHistoryBatchProcessor batchProcessor;
    private final IVideoService videoService;

    @Bean
    public BufferTrigger<Map.Entry<VideoHistoryKey, VideoHistoryData>> videoHistoryBufferTrigger() {
        return BufferTrigger.<Map.Entry<VideoHistoryKey, VideoHistoryData>, Map<VideoHistoryKey, VideoHistoryData>>simple()
                // 1. setContainer usually expects a boolean return: true to trigger, false to continue
                .setContainer(HashMap::new, (map, entry) -> {
                    map.put(entry.getKey(), entry.getValue());
                    return false; // Return false to indicate "don't trigger yet"
                })
                .interval(5, TimeUnit.SECONDS)
                // 2. Ensure triggerStrategy returns the correct TriggerResult
                .triggerStrategy((size, time) -> {
                    if (size >= 100) {
                        // 参数1: true 表示执行 consumer
                        // 参数2: -1 表示不改变下一次执行的时间间隔
                        return SimpleBufferTrigger.TriggerResult.trig(true, -1);
                    }
                    return SimpleBufferTrigger.TriggerResult.empty();
                })
                // 委托给 VideoHistoryBatchProcessor 处理批量保存
                .consumer(batchProcessor::batchSaveVideoHistory)
                .build();
    }
    /**
     * 视频播放次数聚合器
     * 策略：5秒 or 200条数据触发
     */
    @Bean
    public BufferTrigger<Long> videoViewCountBufferTrigger() {
        return BufferTrigger.<Long, Map<Long, Long>>simple()
                .setContainer(HashMap::new, (map, videoId) -> {
                    map.merge(videoId, 1L, Long::sum);
                    // 修复点：返回 false。
                    // 在这个接口定义中，false 表示“放入数据后不立即触发”，
                    // 具体的触发时机由下方的 triggerStrategy 决定。
                    return false;
                })
                .interval(5, TimeUnit.SECONDS)
                .triggerStrategy((size, time) -> {
                    // 这里依然可以使用 TriggerResult，或者如果这里也报错，
                    // 则需要根据 triggerStrategy 要求的接口类型调整。
                    if (size >= 200) {
                        return SimpleBufferTrigger.TriggerResult.trig(true, -1);
                    }
                    return SimpleBufferTrigger.TriggerResult.empty();
                })
                .consumer(this::batchUpdateViewCounts)
                .build();
    }


    /**
     * 批量更新视频播放次数
     */
    private void batchUpdateViewCounts(Map<Long, Long> buffer) {
        if (buffer.isEmpty()) {
            return;
        }

        try {
            // 批量更新数据库
            buffer.forEach((videoId, count) -> {
                videoService.lambdaUpdate()
                        .eq(com.hsx.manyue.modules.video.model.entity.VideoEntity::getId, videoId)
                        .setSql("count = count + " + count)
                        .update();
            });

            log.info("BufferTrigger 批量更新视频播放次数成功，共更新 {} 个视频", buffer.size());
        } catch (Exception e) {
            log.error("BufferTrigger 批量更新视频播放次数失败", e);
        }
    }

    /**
     * 视频观看历史 Key
     */
    public static class VideoHistoryKey {
        private Long userId;
        private Long videoId;
        private String clientId;
        private String ip;

        public VideoHistoryKey(Long userId, Long videoId) {
            this.userId = userId;
            this.videoId = videoId;
        }

        public VideoHistoryKey(String clientId, Long videoId, String ip) {
            this.clientId = clientId;
            this.videoId = videoId;
            this.ip = ip;
        }

        public boolean isGuest() {
            return clientId != null;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getVideoId() {
            return videoId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getIp() {
            return ip;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VideoHistoryKey that = (VideoHistoryKey) o;
            if (isGuest()) {
                return clientId.equals(that.clientId) && videoId.equals(that.videoId) && ip.equals(that.ip);
            } else {
                return userId.equals(that.userId) && videoId.equals(that.videoId);
            }
        }

        @Override
        public int hashCode() {
            if (isGuest()) {
                return (clientId + ":" + videoId + ":" + ip).hashCode();
            } else {
                return (userId + ":" + videoId).hashCode();
            }
        }
    }

    /**
     * 视频观看历史数据
     */
    public static class VideoHistoryData {
        private Double time;

        public VideoHistoryData(Double time) {
            this.time = time;
        }

        public Double getTime() {
            return time;
        }

        public void setTime(Double time) {
            this.time = time;
        }
    }
}
