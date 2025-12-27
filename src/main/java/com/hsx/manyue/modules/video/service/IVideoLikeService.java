package com.hsx.manyue.modules.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.video.model.entity.VideoLikeEntity;

import java.util.Set;

/**
 * 视频点赞表 服务类
 */
public interface IVideoLikeService extends IService<VideoLikeEntity> {

    Long getLikeCount(Long videoId);

    void likeVideo(Long videoId);

    boolean isLikeVideo(Long userId, Long videoId);

    void saveOrUpdateLike(Long videoId, Set<Long> userIds);
}
