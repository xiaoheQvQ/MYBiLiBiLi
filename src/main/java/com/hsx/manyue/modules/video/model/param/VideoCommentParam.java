package com.hsx.manyue.modules.video.model.param;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class VideoCommentParam {
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private Long parentId;
    private Long replyTo;
}