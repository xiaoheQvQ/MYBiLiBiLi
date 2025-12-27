package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoHistoryEntity;

import java.util.List;

/**
 * 视频观看历史记录 Mapper 接口
 */
public interface VideoHistoryMapper extends BaseMapper<VideoHistoryEntity> {

    List<VideoDTO> histories(Long userId);
}
