package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视频点赞表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_video_like")
public class VideoLikeEntity extends BaseEntity<VideoLikeEntity> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 视频ID
     */
    private Long videoId;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
