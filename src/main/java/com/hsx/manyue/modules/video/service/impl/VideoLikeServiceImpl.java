package com.hsx.manyue.modules.video.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.video.mapper.VideoLikeMapper;
import com.hsx.manyue.modules.video.model.entity.VideoLikeEntity;
import com.hsx.manyue.modules.video.service.IVideoLikeService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频点赞表 服务实现类
 */
@Service
public class VideoLikeServiceImpl extends ServiceImpl<VideoLikeMapper, VideoLikeEntity> implements IVideoLikeService {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Long getLikeCount(Long videoId) {
        String key = RedisKeys.VIDEO_LIKE + videoId;
        if (redisUtil.hasKey(key)) {
            return redisUtil.sGetSetSize(key);
        }
        Set<String> set = baseMapper.getUserIdSetByVideoId(videoId);
        if (set.size() == 0) {
            return 0L;
        }
        // 使用redisUtil添加集合元素
        for (String userId : set) {
            redisUtil.sSet(key, userId);
        }
        redisUtil.expire(key, 30, TimeUnit.MINUTES);
        return (long) set.size();
    }

    @Override
    public void likeVideo(Long videoId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String userIdStr = userId.toString();
        String key = RedisKeys.VIDEO_LIKE + videoId;

        // 先保存到数据库
        // 判断当前视频是否存在当前用户的点赞 // 不存在新增  存在删除 并且更新缓存
        List<VideoLikeEntity> doList = list(Wrappers.lambdaQuery(VideoLikeEntity.class).eq(VideoLikeEntity::getVideoId, videoId));
        boolean anyMatch = doList.stream().anyMatch(x -> StrUtil.equals(String.valueOf(x.getUserId()), userIdStr));
        if (anyMatch) {
            // 更新数据库
            remove(Wrappers.lambdaQuery(VideoLikeEntity.class)
                    .eq(VideoLikeEntity::getVideoId, videoId)
                    .eq(VideoLikeEntity::getUserId, userId));
            // 更新缓存
            if (redisUtil.sHasKey(key, userIdStr)) {
                redisUtil.setRemove(key, userIdStr);
            }
        } else {
            save(new VideoLikeEntity().setVideoId(videoId).setUserId(userId));
            redisUtil.sSet(key, userIdStr);
        }
    }

    @Override
    public boolean isLikeVideo(Long userId, Long videoId) {
        this.getLikeCount(videoId);
        // 使用RedisUtil替代RedisTemplate
        String key = RedisKeys.VIDEO_LIKE + videoId;
        String userIdStr = String.valueOf(userId);
        return redisUtil.sHasKey(key, userIdStr);
    }

    @Override
    public void saveOrUpdateLike(Long videoId, Set<Long> userIds) {
        Set<Long> likedUserIds = this.lambdaQuery().eq(VideoLikeEntity::getVideoId, videoId)
                .select(VideoLikeEntity::getUserId)
                .list()
                .stream().map(VideoLikeEntity::getUserId).collect(Collectors.toSet());
        Set<Long> unionUserIds = new HashSet<>(CollUtil.union(likedUserIds, userIds));
        Set<Long> removeUserIds = new HashSet<>(CollUtil.disjunction(unionUserIds, userIds));
        Set<Long> newUserIds = new HashSet<>(CollUtil.disjunction(unionUserIds, likedUserIds));

        if (CollUtil.isNotEmpty(removeUserIds)) {
            baseMapper.delRecord(videoId, removeUserIds);
        }
        List<VideoLikeEntity> entities = newUserIds.stream()
                .map(i -> new VideoLikeEntity().setVideoId(videoId).setUserId(i))
                .collect(Collectors.toList());
        this.saveBatch(entities);
    }
}
