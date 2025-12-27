package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;



@Data
@TableName("t_video_comment")
public class CommentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long videoId;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDelete;
    private Long parentId;

    @TableField(exist = false)
    private Long toUserId;

    @TableField(exist = false)
    private Long replyCommentId;

    @TableField(exist = false)
    private String nick;

    @TableField(exist = false)
    private String videoPublishUserId;

    @TableField(exist = false)
    private String videoTitle;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date toCreateTime;
}
