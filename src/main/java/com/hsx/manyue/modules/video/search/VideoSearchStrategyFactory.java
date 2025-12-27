package com.hsx.manyue.modules.video.search;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 视频搜索策略工厂
 */
@Component
@RequiredArgsConstructor
public class VideoSearchStrategyFactory {

    private final List<VideoSearchStrategy> strategies;

    /**
     * 根据类型选择并执行搜索
     */
    public IPage<VideoDTO> search(VideoQueryParam param) {
        return strategies.stream()
                .filter(s -> s.supports(param.getType()))
                .findFirst()
                .map(s -> s.search(param))
                .orElseThrow(() -> new IllegalArgumentException("不支持的搜索类型: " + param.getType()));
    }
}
