package com.hsx.manyue.modules.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.video.model.dto.TagDTO;
import com.hsx.manyue.modules.video.model.entity.VideoTagEntity;

import java.util.List;

/**
 * 视频标签关联表 服务类
 */
public interface IVideoTagService extends IService<VideoTagEntity> {

    void saveTags(Long videoId, List<TagDTO> tags);

    List<TagDTO> listByVideoId(Long id);
}
