package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.video.model.dto.TagDTO;
import com.hsx.manyue.modules.video.model.entity.VideoTagEntity;
<<<<<<< HEAD
import org.apache.ibatis.annotations.Mapper;
=======
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3

import java.util.List;

/**
 * 视频标签关联表 Mapper 接口
 */
<<<<<<< HEAD
@Mapper
=======
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
public interface VideoTagMapper extends BaseMapper<VideoTagEntity> {

    List<TagDTO> selectListByVideoId(Long id);
}
