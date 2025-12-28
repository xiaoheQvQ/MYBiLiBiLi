package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import com.hsx.manyue.common.enums.VideoStatusEnum;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视频表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_video")
public class VideoEntity extends BaseEntity<VideoEntity> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 阿里云视频ID
     */
    private String videoId;

    /**
     * 视频MD5
     */
    private String md5;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    private String cover;

    /**
     * 观看次数
     */
    private Long count;

    /**
     * 点赞数量
     */
    @TableField(exist = false)
    private Long like;

    private Float duration;

    private VideoAreaEnum area;

    private VideoStatusEnum status;

    private String subtitle;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
