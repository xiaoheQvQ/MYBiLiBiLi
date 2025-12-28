package com.hsx.manyue.common.service;

import com.hsx.manyue.common.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis Bitmap点赞服务
 * 优化点：使用Bitmap替代Set，大幅减少内存占用，提升查询性能
 * 
 * Bitmap优势：
 * 1. 内存占用：1亿用户的点赞数据仅需约12MB（vs Set需要约400MB）
 * 2. 查询性能：O(1)时间复杂度
 * 3. 批量操作：支持高效的位运算
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BitmapLikeService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 用户点赞/取消点赞
     * @param type 类型（post/video/note等）
     * @param contentId 内容ID
     * @param userId 用户ID
     * @return true=点赞，false=取消点赞
     */
    public boolean toggleLike(String type, Long contentId, Long userId) {
        String key = getKey(type, contentId);
        Boolean isLiked = redisTemplate.opsForValue().getBit(key, userId);
        
        if (Boolean.TRUE.equals(isLiked)) {
            // 取消点赞
            redisTemplate.opsForValue().setBit(key, userId, false);
            return false;
        } else {
            // 点赞
            redisTemplate.opsForValue().setBit(key, userId, true);
            return true;
        }
    }

    /**
     * 检查用户是否点赞
     * @param type 类型
     * @param contentId 内容ID
     * @param userId 用户ID
     * @return true=已点赞
     */
    public boolean isLiked(String type, Long contentId, Long userId) {
        String key = getKey(type, contentId);
        Boolean result = redisTemplate.opsForValue().getBit(key, userId);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 获取点赞数量（估算）
     * 注意：BITCOUNT性能较好，但对于海量数据建议单独维护计数器
     * @param type 类型
     * @param contentId 内容ID
     * @return 点赞数量
     */
    public Long getLikeCount(String type, Long contentId) {
        String key = getKey(type, contentId);
        return redisTemplate.execute((connection) -> {
            return connection.stringCommands().bitCount(key.getBytes());
        }, true);
    }

    /**
     * 批量检查用户点赞状态
     * @param type 类型
     * @param contentId 内容ID
     * @param userIds 用户ID列表
     * @return 点赞状态列表
     */
    public List<Boolean> batchCheckLiked(String type, Long contentId, List<Long> userIds) {
        String key = getKey(type, contentId);
        List<Boolean> results = new ArrayList<>();
        
        for (Long userId : userIds) {
            Boolean isLiked = redisTemplate.opsForValue().getBit(key, userId);
            results.add(Boolean.TRUE.equals(isLiked));
        }
        
        return results;
    }

    /**
     * 获取已点赞的用户ID列表（适用于小数据量）
     * 注意：此方法会遍历整个Bitmap，不适合大数据量场景
     * @param type 类型
     * @param contentId 内容ID
     * @param maxUserId 最大用户ID（用于限制遍历范围）
     * @return 用户ID列表
     */
    public List<Long> getLikedUserIds(String type, Long contentId, Long maxUserId) {
        String key = getKey(type, contentId);
        List<Long> userIds = new ArrayList<>();
        
        for (long userId = 0; userId <= maxUserId; userId++) {
            Boolean isLiked = redisTemplate.opsForValue().getBit(key, userId);
            if (Boolean.TRUE.equals(isLiked)) {
                userIds.add(userId);
            }
        }
        
        return userIds;
    }

    /**
     * 清除内容的所有点赞数据
     * @param type 类型
     * @param contentId 内容ID
     */
    public void clearLikes(String type, Long contentId) {
        String key = getKey(type, contentId);
        redisTemplate.delete(key);
    }

    /**
     * 构建Redis Key
     */
    private String getKey(String type, Long contentId) {
        switch (type.toLowerCase()) {
            case "post":
                return RedisKeys.POST_LIKE_BITMAP + contentId;
            case "note":
                return RedisKeys.NOTE_LIKE_BITMAP + contentId;
            default:
                throw new IllegalArgumentException("不支持的类型: " + type);
        }
    }
}
