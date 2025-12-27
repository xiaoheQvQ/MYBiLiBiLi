package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoCollectionEntity;

import java.util.List;

/**
 * 视频收藏表 Mapper 接口
 */
public interface VideoCollectionMapper extends BaseMapper<VideoCollectionEntity> {

    List<VideoDTO> getCollections(Long userId);
}
