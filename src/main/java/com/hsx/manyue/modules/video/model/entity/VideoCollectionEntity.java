package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视频收藏表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_video_collection")
public class VideoCollectionEntity extends BaseEntity<VideoCollectionEntity> {

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
