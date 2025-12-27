package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_video_comment")
public class VideoCommentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long videoId;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    private Long replyUserId;

    private Long rootId = -1L;
}