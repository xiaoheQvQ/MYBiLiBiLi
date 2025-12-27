package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.video.model.dto.TagDTO;
import com.hsx.manyue.modules.video.model.entity.TagEntity;

import java.util.List;

/**
 * 视频标签表 Mapper 接口
 */
public interface TagMapper extends BaseMapper<TagEntity> {

    List<TagDTO> getTags();
}
