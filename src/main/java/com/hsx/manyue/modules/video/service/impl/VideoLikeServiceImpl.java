package com.hsx.manyue.modules.video.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.service.DistributedLockService;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.video.mapper.VideoLikeMapper;
import com.hsx.manyue.modules.video.model.entity.VideoLikeEntity;
import com.hsx.manyue.modules.video.service.IVideoLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频点赞表 服务实现类
 * 优化：使用分布式锁保证高并发场景下的数据一致性
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoLikeServiceImpl extends ServiceImpl<VideoLikeMapper, VideoLikeEntity> implements IVideoLikeService {

    @Resource
    private RedisUtil redisUtil;
    
    private final DistributedLockService lockService;

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

    /**
     * 点赞/取消点赞视频（优化版：使用分布式锁）
     * 解决高并发场景下的数据一致性问题
     */
    @Override
    public void likeVideo(Long videoId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String lockKey = "video:like:" + videoId + ":" + userId;
        
        // 使用分布式锁保证数据一致性（最多等待2秒，锁持有5秒）
        lockService.executeWithLockRetry(lockKey, 5, 2000, 50, () -> {
            String userIdStr = userId.toString();
            String key = RedisKeys.VIDEO_LIKE + videoId;

            // 判断当前视频是否存在当前用户的点赞
            List<VideoLikeEntity> doList = list(Wrappers.lambdaQuery(VideoLikeEntity.class)
                    .eq(VideoLikeEntity::getVideoId, videoId));
            boolean anyMatch = doList.stream()
                    .anyMatch(x -> StrUtil.equals(String.valueOf(x.getUserId()), userIdStr));
            
            if (anyMatch) {
                // 取消点赞：先更新数据库，再更新缓存
                remove(Wrappers.lambdaQuery(VideoLikeEntity.class)
                        .eq(VideoLikeEntity::getVideoId, videoId)
                        .eq(VideoLikeEntity::getUserId, userId));
                
                // 更新缓存
                if (redisUtil.sHasKey(key, userIdStr)) {
                    redisUtil.setRemove(key, userIdStr);
                }
                log.debug("用户 {} 取消点赞视频 {}", userId, videoId);
            } else {
                // 点赞：先更新数据库，再更新缓存
                save(new VideoLikeEntity().setVideoId(videoId).setUserId(userId));
                redisUtil.sSet(key, userIdStr);
                log.debug("用户 {} 点赞视频 {}", userId, videoId);
            }
            
            return null;
        });
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
