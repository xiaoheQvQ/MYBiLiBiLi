package com.hsx.manyue.modules.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoCollectionEntity;

import java.util.List;

/**
 * 视频收藏表 服务类
 */
public interface IVideoCollectionService extends IService<VideoCollectionEntity> {

    void collect(Long videoId);

    boolean isCollect(Long userId, Long videoId);

    List<VideoDTO> getCollections(Long userId);
}
