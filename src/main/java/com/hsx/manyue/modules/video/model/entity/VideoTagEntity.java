package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视频标签关联表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_video_tag")

public class VideoTagEntity extends BaseEntity<VideoTagEntity> {

    private static final long serialVersionUID = 1L;

    private Long videoId;

    private Long tagId;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
