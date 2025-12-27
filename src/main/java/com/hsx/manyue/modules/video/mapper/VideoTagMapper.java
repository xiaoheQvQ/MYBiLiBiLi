package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.video.model.dto.TagDTO;
import com.hsx.manyue.modules.video.model.entity.VideoTagEntity;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

/**
 * 视频标签关联表 Mapper 接口
 */
@Mapper
public interface VideoTagMapper extends BaseMapper<VideoTagEntity> {

    List<TagDTO> selectListByVideoId(Long id);
}
