package com.hsx.manyue.modules.video.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.model.entity.BaseEntity;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.danmaku.model.dto.DplayerDanmakuDTO;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;
import com.hsx.manyue.modules.danmaku.service.IDanmakuService;
import com.hsx.manyue.modules.oss.service.IVodService;
import com.hsx.manyue.modules.user.service.IUserSubscriptionService;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频相关信息定时任务类
 */
@Component
@Slf4j
public class VideoInfoScheduledTask {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IVideoService videoService;

    @Resource
    private IVodService vodService;

    @Resource
    private IUserSubscriptionService userSubscriptionService;

    private final ThreadPoolExecutor persistenceDanmakuExecutor = new ThreadPoolExecutor(1,
            5,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(10),
            ThreadFactoryBuilder.create().setNamePrefix("persistence-danmaku").build());

    @Resource
    private IDanmakuService danmakuService;

    @Resource
    private IVideoLikeService videoLikeService;

//    @Scheduled(cron = "0 0,30 * * * ?")
//    @PostConstruct
//    @Transactional(rollbackFor = Exception.class)
//    public void syncVideoData() {
//        List<VideoLikeEntity> list = videoLikeService.list();
//        for (VideoLikeEntity likeEntity : list) {
//            String key = RedisKeys.VIDEO_LIKE + likeEntity.getVideoId();
//            redisUtil.lSet(key, likeEntity.getUserId());
//        }
//    }

    public Set<Long> syncViewCounts() {
        log.info("开始同步视屏观看次数");
        Map<Long, VideoEntity> entityMap = new HashMap<>();

        // 使用RedisTemplate获取所有匹配的keys
        Set<String> viewCounts = redisTemplate.keys(RedisKeys.VIEW_COUNTS + "*");

        for (String key : viewCounts) {
            Long videoId = Long.valueOf(key.split(":")[2]);
            VideoEntity videoEntity;
            if (!entityMap.containsKey(videoId)) {
                videoEntity = new VideoEntity();
                videoEntity.setId(videoId);
            } else {
                videoEntity = entityMap.get(videoId);
            }

            // 使用redisUtil获取值并删除
            Object value = redisUtil.get(key);
            redisUtil.del(key);
            videoEntity.setCount(Long.valueOf(value.toString()));
            entityMap.put(videoId, videoEntity);
        }
        videoService.updateBatchById(entityMap.values());
        log.info("结束同步视屏观看次数");
        return entityMap.values().stream().map(BaseEntity::getId).collect(Collectors.toSet());
    }

    public Set<Long> syncLikeCounts() {
        log.info("开始同步视频点赞数据");

        // 使用RedisTemplate获取所有匹配的keys
        Set<String> keys = redisTemplate.keys(RedisKeys.VIDEO_LIKE + "*");

        Set<Long> videoIds = new HashSet<>();
        for (String key : keys) {
            // 使用redisUtil获取集合成员
            Set<Object> userIdObjSet = redisUtil.sGet(key);
            Set<String> userIdStrSet = userIdObjSet.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());

            Long videoId = Long.valueOf(key.split(":")[2]);
            Set<Long> userIds = userIdStrSet.stream().map(Long::valueOf).collect(Collectors.toSet());
            videoLikeService.saveOrUpdateLike(videoId, userIds);
            videoIds.add(videoId);
        }
        log.info("结束同步视频点赞数据");
        return videoIds;
    }

    public void syncDanmakus() {
        // 使用RedisTemplate获取所有匹配的keys
        Set<String> keys = redisTemplate.keys(RedisKeys.DANMAKU_NEW.concat("*"));

        log.info("定时持久化弹幕数据，共有{}个视频的弹幕需要持久化", keys.size());

        for (String key : keys) {
            // 使用redisUtil获取列表长度和内容
            long size = redisUtil.lGetListSize(key);
            List<Object> valuesObj = redisUtil.lGet(key, 0, size - 1);

            // 清空列表
            redisUtil.del(key);

            List<String> values = valuesObj.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            if (CollectionUtil.isEmpty(values)) {
                continue;
            }

            persistenceDanmakuExecutor.execute(() -> {
                log.info("{}开始持久化弹幕，共有{}条弹幕", Thread.currentThread().getName(), values.size());
                List<DanmakuEntity> entities = new ArrayList<>();
                for (String value : values) {
                    DplayerDanmakuDTO dto = JSONUtil.toBean(value, DplayerDanmakuDTO.class);
                    DanmakuEntity danmakuEntity = new DanmakuEntity()
                            .setTime(dto.getTime())
                            .setNick(dto.getAuthor())
                            .setColor(dto.getColor())
                            .setContent(dto.getText())
                            .setPosition(dto.getType())
                            .setUserId(dto.getUserId())
                            .setVideoId(dto.getPlayer());
                    entities.add(danmakuEntity);
                }
                danmakuService.saveBatch(entities);
                log.info("{}完成弹幕持久化", Thread.currentThread().getName());
            });
        }
    }

//    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
//    public void updateVideoReviewStatus() throws ClientException {
//        // 获取列表长度
//        long size = redisUtil.lGetListSize(RedisKeys.AI_REVIEW);
//        for (int i = 0; i < size; i++) {
//            // 获取第一个元素并移除
//            Object valueObj = redisUtil.lGetIndex(RedisKeys.AI_REVIEW, 0);
//            redisUtil.lRemove(RedisKeys.AI_REVIEW, 1, valueObj);
//            String jsonStr = valueObj.toString();
//
//            AiReviewJobDTO job = JSONUtil.toBean(jsonStr, AiReviewJobDTO.class);
//
//            String reviewResult = vodService.getReviewResult(job.getJobId());
//            if (StrUtil.equals(reviewResult, AliyunAiJobStatusConstant.JOB_FAIL)) {
//                log.error("视频：{} AI审核失败！", job.getVideoId());
//                continue;
//            }
//            if (StrUtil.equals(reviewResult, AliyunAiJobStatusConstant.PASS) ||
//                    StrUtil.equals(reviewResult, AliyunAiJobStatusConstant.REVIEW)) {
//                log.info("视频：{} AI 审核成功", job.getVideoId());
//                GetVideoInfoResponse.Video videoInfo = vodService.getVideoInfo(job.getAliyunVideoId());
//                videoService.lambdaUpdate()
//                        .eq(VideoEntity::getId, job.getVideoId())
//                        .set(VideoEntity::getStatus, VideoStatusEnum.PUBLISHED)
//                        .set(VideoEntity::getDuration, videoInfo.getDuration())
//                        .update();
//                redisUtil.del(RedisKeys.VIDEO_DETAIL + job.getVideoId());
//                userSubscriptionService.publishVideo(job.getVideoId());
//                videoService.saveToEsById(job.getVideoId());
//            } else if (StrUtil.equals(reviewResult, AliyunAiJobStatusConstant.BLOCK)) {
//                log.info("视频：{} AI 审核违规", job.getVideoId());
//                videoService.lambdaUpdate()
//                        .eq(VideoEntity::getId, job.getVideoId())
//                        .set(VideoEntity::getStatus, VideoStatusEnum.BLOCK)
//                        .update();
//                redisUtil.del(RedisKeys.VIDEO_DETAIL + job.getVideoId());
//            } else {
//                // 重新放回列表尾部
//                redisUtil.lSet(RedisKeys.AI_REVIEW, jsonStr);
//            }
//        }
//    }
}
