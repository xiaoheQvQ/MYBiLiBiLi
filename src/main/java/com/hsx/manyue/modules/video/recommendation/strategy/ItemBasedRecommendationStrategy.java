package com.hsx.manyue.modules.video.recommendation.strategy;

import com.hsx.manyue.common.utils.IpUtil;
import com.hsx.manyue.modules.video.mapper.VideoMapper;
import com.hsx.manyue.modules.video.model.dto.UserPreference;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.recommendation.RecommendationContext;
import com.hsx.manyue.modules.video.recommendation.VideoRecommendationStrategy;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.stereotype.Component;
import cn.hutool.core.collection.CollUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于内容的推荐策略
 * 
 * 适用场景：游客用户或新用户（无登录状态）
 * 算法原理：根据用户最近浏览的视频，推荐与之相似的其他视频
 * 
 * 符合单一职责原则：只负责基于内容的协同过滤推荐
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItemBasedRecommendationStrategy implements VideoRecommendationStrategy {
    
    private final VideoMapper videoMapper;
    
    /**
     * 最小偏好数据量阈值
     */
    private static final int MIN_PREFERENCE_COUNT = 5;
    
    /**
     * 推荐数量
     */
    private static final int RECOMMEND_COUNT = 50;
    
    @Override
    public boolean isApplicable(RecommendationContext context) {
        // 对游客用户（未登录）适用
        return context.getUserId() == null;
    }
    
    @Override
    public List<VideoDTO> recommend(RecommendationContext context) throws TasteException {
        log.info("执行基于内容的推荐（游客模式）");
        
        // 获取推荐种子视频
        Long seedVideoId = getSeedVideoId(context.getRequest());
        
        // 检查种子视频的偏好数据量
        Long count = videoMapper.getPreferenceCountByVideoId(seedVideoId);
        if (count < MIN_PREFERENCE_COUNT) {
            log.info("种子视频 {} 偏好数据不足（{}条），使用随机推荐", seedVideoId, count);
            return getRandomVideos();
        }
        
        try {
            // 执行基于内容的协同过滤
            DataModel dataModel = buildDataModel();
            ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
            GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(
                dataModel, similarity
            );
            
            log.info("为游客用户提供基于视频 {} 的推荐", seedVideoId);
            List<RecommendedItem> recommendedItems = recommender.mostSimilarItems(
                seedVideoId, RECOMMEND_COUNT
            );
            
            if (recommendedItems.isEmpty()) {
                log.info("视频 {} 可推荐数据太少，使用随机推荐", seedVideoId);
                return getRandomVideos();
            }
            
            // 转换为视频DTO
            log.info("成功推荐 {} 个相关视频", recommendedItems.size());
            List<Long> videoIds = recommendedItems.stream()
                .map(RecommendedItem::getItemID)
                .collect(Collectors.toList());
            
            return getRandomVideo(videoIds);
            
        } catch (NoSuchItemException e) {
            log.info("视频 {} 相关数据稀少，使用随机推荐", seedVideoId);
            return getRandomVideos();
        }
    }
    
    /**
     * 获取推荐种子视频ID
     * 
     * 优先级：
     * 1. 客户端ID对应的热门视频
     * 2. 同IP用户的热门视频
     * 3. 全站热门视频
     * 
     * @param request HTTP请求对象
     * @return 种子视频ID
     */
    private Long getSeedVideoId(HttpServletRequest request) {
        // 解析客户端信息
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);
        
        List<Map<String, Object>> hotVideos = null;
        
        // 优先根据客户端ID获取热门视频
        if (clientId != null) {
            hotVideos = videoMapper.selectHotVideosByClientId(clientId, 1);
            if (!hotVideos.isEmpty()) {
                Long videoId = (Long) hotVideos.get(0).get("video_id");
                log.info("根据客户端ID {} 获取种子视频: {}", clientId, videoId);
                return videoId;
            }
        }
        
        // 次之根据IP获取热门视频
        if (ip != null) {
            hotVideos = videoMapper.selectHotVideosByIp(ip, 1);
            if (!hotVideos.isEmpty()) {
                Long videoId = (Long) hotVideos.get(0).get("video_id");
                log.info("根据IP {} 获取种子视频: {}", ip, videoId);
                return videoId;
            }
        }
        
        // 最后返回全站热门视频
        Long globalHotVideo = videoMapper.selectGlobalHotVideo();
        log.info("使用全站热门视频作为种子: {}", globalHotVideo);
        return globalHotVideo;
    }
    
    /**
     * 构建协同过滤数据模型
     */
    private DataModel buildDataModel() {
        List<UserPreference> userPreferences = videoMapper.getPreferenceData();
        
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        List<List<UserPreference>> userPreferencesGroupByUserId = 
            CollUtil.groupByField(userPreferences, "userId");
        
        for (List<UserPreference> preferences : userPreferencesGroupByUserId) {
            PreferenceArray preferenceArray = new GenericUserPreferenceArray(preferences);
            fastByIdMap.put(preferences.get(0).getUserID(), preferenceArray);
        }
        
        return new GenericDataModel(fastByIdMap);
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
}
