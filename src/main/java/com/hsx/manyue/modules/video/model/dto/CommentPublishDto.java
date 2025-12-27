package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CommentPublishDto {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 255, message = "评论内容不能超过255个字符")
    private String content;
}