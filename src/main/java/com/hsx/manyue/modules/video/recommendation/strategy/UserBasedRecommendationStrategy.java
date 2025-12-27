package com.hsx.manyue.modules.video.recommendation.strategy;

import cn.hutool.core.collection.CollUtil;
import com.hsx.manyue.modules.video.mapper.VideoMapper;
import com.hsx.manyue.modules.video.model.dto.UserPreference;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.recommendation.RecommendationContext;
import com.hsx.manyue.modules.video.recommendation.VideoRecommendationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基于用户的协同过滤推荐策略
 * 
 * 适用场景：已登录用户且有足够的历史偏好数据
 * 算法原理：找到与目标用户兴趣相似的其他用户，推荐这些相似用户喜欢的视频
 * 
 * 符合单一职责原则：只负责基于用户的协同过滤推荐
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserBasedRecommendationStrategy implements VideoRecommendationStrategy {
    
    private final VideoMapper videoMapper;
    
    /**
     * 最小偏好数据量阈值
     * 用户的历史偏好数据少于此值时，降级到随机推荐
     */
    private static final int MIN_PREFERENCE_COUNT = 5;
    
    /**
     * 默认推荐数量
     */
    private static final int DEFAULT_RECOMMEND_COUNT = 50;
    
    /**
     * 最终返回的推荐视频数量
     */
    private static final int FINAL_RECOMMEND_COUNT = 7;
    
    @Override
    public boolean isApplicable(RecommendationContext context) {
        // 只对已登录用户适用
        return context.getUserId() != null;
    }
    
    @Override
    public List<VideoDTO> recommend(RecommendationContext context) throws TasteException {
        Long userId = context.getUserId();
        
        // 检查用户偏好数据量
        Long count = videoMapper.getPreferenceCountByUserId(userId);
        if (count < MIN_PREFERENCE_COUNT) {
            log.info("用户 {} 偏好数据不足（{}条），降级到随机推荐", userId, count);
            return getRandomVideos();
        }
        
        // 执行协同过滤算法
        log.info("为用户 {} 执行基于用户的协同过滤推荐", userId);
        DataModel dataModel = buildDataModel();
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(
            dataModel, neighborhood, similarity
        );
        
        // 获取推荐结果
        List<RecommendedItem> items = recommender.recommend(userId, DEFAULT_RECOMMEND_COUNT);
        if (items.isEmpty()) {
            log.info("用户 {} 可推荐视频数据太少，使用随机推荐", userId);
            return getRandomVideos();
        }
        
        // 转换为视频DTO并补充不足的推荐数量
        return convertAndFillRecommendations(items, userId);
    }
    
    /**
     * 构建协同过滤数据模型
     * 
     * @return Mahout 数据模型
     */
    private DataModel buildDataModel() {
        // 从数据库获取所有用户的偏好数据
        List<UserPreference> userPreferences = videoMapper.getPreferenceData();
        
        // 构建 FastByIDMap 用于存储用户偏好
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        
        // 按用户ID分组
        List<List<UserPreference>> userPreferencesGroupByUserId = 
            CollUtil.groupByField(userPreferences, "userId");
        
        // 为每个用户创建偏好数组
        for (List<UserPreference> preferences : userPreferencesGroupByUserId) {
            PreferenceArray preferenceArray = new GenericUserPreferenceArray(preferences);
            fastByIdMap.put(preferences.get(0).getUserID(), preferenceArray);
        }
        
        return new GenericDataModel(fastByIdMap);
    }
    
    /**
     * 转换推荐结果并补充不足的推荐数量
     * 
     * @param items 推荐的视频项
     * @param userId 用户ID
     * @return 最终的推荐视频列表
     */
    private List<VideoDTO> convertAndFillRecommendations(List<RecommendedItem> items, Long userId) {
        // 提取视频ID并去重
        List<Long> videoIds = items.stream()
            .map(RecommendedItem::getItemID)
            .distinct()
            .collect(Collectors.toList());
        
        // 计算还需要补充的视频数量
        int needVideoSize = FINAL_RECOMMEND_COUNT - videoIds.size();
        if (needVideoSize > 0) {
            log.info("推荐结果不足，需要补充 {} 个视频", needVideoSize);
            // 获取补充的视频ID，并确保不重复
            List<Long> additionalVideoIds = getRandomNeedVideo(needVideoSize, userId)
                .stream()
                .map(VideoDTO::getId)
                .filter(id -> !videoIds.contains(id))
                .collect(Collectors.toList());
            
            videoIds.addAll(additionalVideoIds);
        }
        
        // 如果最终视频数量仍不足，继续补充
        while (videoIds.size() < FINAL_RECOMMEND_COUNT) {
            List<Long> extraVideoIds = getRandomNeedVideo(1, userId)
                .stream()
                .map(VideoDTO::getId)
                .filter(id -> !videoIds.contains(id))
                .collect(Collectors.toList());
            
            if (!extraVideoIds.isEmpty()) {
                videoIds.add(extraVideoIds.get(0));
            } else {
                break; // 无法获取更多视频，退出循环
            }
        }
        
        // 根据视频ID获取完整的视频信息
        return Optional.ofNullable(getRandomVideo(videoIds))
            .orElseGet(this::getRandomVideos);
    }
    
    /**
     * 获取随机视频（降级方案）
     */
    private List<VideoDTO> getRandomVideos() {
        return videoMapper.getRandomVideo(Collections.emptyList());
    }
    
    /**
     * 根据视频ID列表获取视频信息
     */
    private List<VideoDTO> getRandomVideo(List<Long> videoIds) {
        return videoMapper.getRandomVideo(videoIds);
    }
    
    /**
     * 获取指定数量的随机视频（排除用户已看过的）
     */
    private List<VideoDTO> getRandomNeedVideo(int needVideoSize, Long userId) {
        return videoMapper.getRandomNeedVideo(needVideoSize, userId);
    }
}
