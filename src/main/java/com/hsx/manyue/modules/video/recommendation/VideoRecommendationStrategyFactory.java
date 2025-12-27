package com.hsx.manyue.modules.video.recommendation;

import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 视频推荐策略工厂
 * 
 * 负责根据推荐上下文选择合适的推荐策略
 * 符合依赖倒置原则：依赖抽象（VideoRecommendationStrategy接口）而非具体实现
 * 
 * 使用 Spring 自动注入所有策略实现，无需手动维护策略列表
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoRecommendationStrategyFactory {
    
    /**
     * Spring 自动注入所有 VideoRecommendationStrategy 的实现类
     * 这样新增策略时无需修改此类，符合开闭原则
     */
    private final List<VideoRecommendationStrategy> strategies;
    
    /**
     * 根据上下文选择最合适的推荐策略并执行推荐
     * 
     * @param context 推荐上下文
     * @return 推荐的视频列表
     * @throws TasteException 推荐算法执行异常
     * @throws IllegalStateException 当没有找到适用的策略时抛出
     */
    public List<VideoDTO> recommend(RecommendationContext context) throws TasteException {
        // 选择第一个适用的策略
        VideoRecommendationStrategy strategy = selectStrategy(context);
        
        // 执行推荐
        log.info("使用策略: {} 执行推荐", strategy.getClass().getSimpleName());
        return strategy.recommend(context);
    }
    
    /**
     * 选择适用的推荐策略
     * 
     * @param context 推荐上下文
     * @return 选中的推荐策略
     * @throws IllegalStateException 当没有找到适用的策略时抛出
     */
    private VideoRecommendationStrategy selectStrategy(RecommendationContext context) {
        return strategies.stream()
            .filter(strategy -> strategy.isApplicable(context))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("没有找到适用的推荐策略"));
    }
}
