package com.hsx.manyue.modules.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.video.model.dto.TagDTO;
import com.hsx.manyue.modules.video.model.entity.TagEntity;

import java.util.List;

/**
 * 视频标签表 服务类
 */
public interface ITagService extends IService<TagEntity> {

    List<TagDTO> getTags();
}
