package com.hsx.manyue.modules.video.search;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;

/**
 * 视频搜索策略接口
 * 遵循策略模式，消除复杂的 if-else 逻辑
 */
public interface VideoSearchStrategy {
    
    /**
     * 执行搜索
     * @param param 搜索参数
     * @return 分页结果
     */
    IPage<VideoDTO> search(VideoQueryParam param);

    /**
     * 判断当前策略是否适用于该搜索请求
     * @param type 搜索类型 (user/video等)
     * @return 是否支持
     */
    boolean supports(String type);
}
