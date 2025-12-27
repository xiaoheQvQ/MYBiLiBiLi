package com.hsx.manyue.common.config;

import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.video.model.entity.VideoLikeEntity;
import com.hsx.manyue.modules.video.service.IVideoLikeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemCacheLoader implements ApplicationRunner {
    @Resource
    private IVideoLikeService videoLikeService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void run(ApplicationArguments args) {
        List<VideoLikeEntity> list = videoLikeService.list();
        // 按视频ID分组处理
        Map<Long, List<Long>> videoLikesMap = new HashMap<>();

        // 收集每个视频的所有点赞用户ID
        for (VideoLikeEntity likeEntity : list) {
            Long videoId = likeEntity.getVideoId();
            Long userId = likeEntity.getUserId();

            if (!videoLikesMap.containsKey(videoId)) {
                videoLikesMap.put(videoId, new ArrayList<>());
            }
            videoLikesMap.get(videoId).add(userId);
        }

        // 批量处理每个视频的点赞记录
        for (Map.Entry<Long, List<Long>> entry : videoLikesMap.entrySet()) {
            Long videoId = entry.getKey();
            List<Long> userIds = entry.getValue();

            String key = RedisKeys.VIDEO_LIKE + videoId;
            // 先删除之前可能存在的键
            redisUtil.del(key);

            // 将每个用户ID转为字符串并添加到集合
            for (Long userId : userIds) {
                redisUtil.sSet(key, userId.toString());
            }
        }
    }
}
