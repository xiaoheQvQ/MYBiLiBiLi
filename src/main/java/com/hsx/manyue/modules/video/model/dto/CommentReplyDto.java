package com.hsx.manyue.modules.video.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Data
public class CommentReplyDto {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
    
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;
    
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 255, message = "回复内容不能超过255个字符")
    private String content;

    private Long replyToUserId;

    private Long replyCommentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date toCreateTime;
}