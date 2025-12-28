package com.hsx.manyue.modules.video.service.impl;

import com.github.phantomthief.collection.BufferTrigger;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.service.IVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCountBufferService {

    private final IVideoService videoService;
    private final RedisTemplate<String, String> redisTemplate;
    private final com.hsx.manyue.common.service.DelayedDoubleDeleteService delayedDoubleDeleteService;

    /**
     * 针对 0.2.21 版本：
     * 变量必须声明为双泛型：<入队类型, 容器类型>
     */
    private final BufferTrigger<Long> viewCountBuffer =
            BufferTrigger.<Long, List<Long>>simple() // 1. 这里必须显式写出两个泛型
                    .maxBufferCount(200)
                    .interval(5, TimeUnit.SECONDS)
                    .consumer(this::processViewCountBuffer) // 2. 这里的引用必须指向接收 List<Long> 的方法
                    .build();

    private final BufferTrigger<Long> likeCountBuffer =
            BufferTrigger.<Long, List<Long>>simple()
                    .maxBufferCount(100)
                    .interval(5, TimeUnit.SECONDS)
                    .consumer(this::processLikeCountBuffer)
                    .build();

    /**
     * 消费者方法签名：必须严格匹配变量定义的第二个泛型参数 R (List<Long>)
     */
    private void processViewCountBuffer(List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) return;
        Map<Long, Long> countMap = videoIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        batchUpdateViewCounts(countMap);
    }

    private void processLikeCountBuffer(List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) return;
        Map<Long, Long> countMap = videoIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        batchUpdateLikeCounts(countMap);
    }

    // --- 业务方法 ---

    public void incrementViewCount(Long videoId, Long count) {
        String key = RedisKeys.VIEW_COUNTS + videoId;
        redisTemplate.opsForValue().increment(key, count);
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        redisTemplate.opsForSet().add(RedisKeys.VIEW_COUNTS_PENDING_KEYS, videoId.toString());

        for (long i = 0; i < count; i++) {
            viewCountBuffer.enqueue(videoId);
        }
    }

    public void incrementLikeCount(Long videoId, Long count) {
        String key = RedisKeys.VIDEO_LIKE + videoId;
        redisTemplate.opsForValue().increment(key, count);
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        redisTemplate.opsForSet().add(RedisKeys.VIDEO_LIKE_PENDING_KEYS, videoId.toString());

        for (long i = 0; i < count; i++) {
            likeCountBuffer.enqueue(videoId);
        }
    }

    private void batchUpdateViewCounts(Map<Long, Long> buffer) {
        if (buffer.isEmpty()) return;
        try {
            delayedDoubleDeleteService.batchDoubleDelete(
                    buffer.keySet().stream().map(id -> RedisKeys.VIEW_COUNTS + id).toList(),
                    () -> {
                        buffer.forEach((videoId, count) -> {
                            videoService.lambdaUpdate()
                                    .eq(VideoEntity::getId, videoId)
                                    .setSql("view_count = view_count + " + count)
                                    .update();
                        });
                    }
            );
            buffer.keySet().forEach(id -> redisTemplate.opsForSet().remove(RedisKeys.VIEW_COUNTS_PENDING_KEYS, id.toString()));
        } catch (Exception e) {
            log.error("批量更新播放量失败", e);
        }
    }

    private void batchUpdateLikeCounts(Map<Long, Long> buffer) {
        if (buffer.isEmpty()) return;
        try {
            delayedDoubleDeleteService.batchDoubleDelete(
                    buffer.keySet().stream().map(id -> RedisKeys.VIDEO_LIKE + id).toList(),
                    () -> {
                        buffer.forEach((videoId, count) -> {
                            videoService.lambdaUpdate()
                                    .eq(VideoEntity::getId, videoId)
                                    .setSql("`like` = `like` + " + count)
                                    .update();
                        });
                    }
            );
            buffer.keySet().forEach(id -> redisTemplate.opsForSet().remove(RedisKeys.VIDEO_LIKE_PENDING_KEYS, id.toString()));
        } catch (Exception e) {
            log.error("批量更新点赞失败", e);
        }
    }

    public void manualFlush() {
        viewCountBuffer.manuallyDoTrigger();
        likeCountBuffer.manuallyDoTrigger();
    }
}