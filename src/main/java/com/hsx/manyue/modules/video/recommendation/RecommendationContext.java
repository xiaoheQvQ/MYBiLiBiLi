package com.hsx.manyue.modules.video.recommendation;

import com.hsx.manyue.modules.video.mapper.VideoMapper;
import lombok.Builder;
import lombok.Data;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 推荐上下文 - 封装推荐所需的所有信息
 * 
 * 使用建造者模式构建上下文对象，便于扩展和维护
 * 
 * @author 系统
 * @since 1.0
 */
@Data
@Builder
public class RecommendationContext {
    
    /**
     * 用户ID（已登录用户）
     * null 表示游客用户
     */
    private Long userId;
    
    /**
     * HTTP 请求对象
     * 用于获取客户端信息（User-Agent、IP等）
     */
    private HttpServletRequest request;
    
    /**
     * 视频数据访问对象
     */
    private VideoMapper videoMapper;
    
    /**
     * 推荐视频数量
     */
    private int recommendCount;
}
