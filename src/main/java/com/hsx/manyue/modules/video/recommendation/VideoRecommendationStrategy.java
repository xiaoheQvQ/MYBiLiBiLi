package com.hsx.manyue.modules.video.recommendation;

import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import org.apache.mahout.cf.taste.common.TasteException;

import java.util.List;

/**
 * 视频推荐策略接口
 * 
 * 定义推荐算法的统一接口，符合开闭原则：对扩展开放，对修改关闭
 * 新增推荐算法只需实现此接口，无需修改现有代码
 * 
 * @author 系统
 * @since 1.0
 */
public interface VideoRecommendationStrategy {
    
    /**
     * 执行推荐算法
     * 
     * @param context 推荐上下文（包含用户ID、请求信息等）
     * @return 推荐的视频列表
     * @throws TasteException 推荐算法执行异常
     */
    List<VideoDTO> recommend(RecommendationContext context) throws TasteException;
    
    /**
     * 判断该策略是否适用于当前上下文
     * 
     * @param context 推荐上下文
     * @return true 表示该策略适用，false 表示不适用
     */
    boolean isApplicable(RecommendationContext context);
}
