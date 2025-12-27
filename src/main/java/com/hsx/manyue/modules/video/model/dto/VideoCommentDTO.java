package com.hsx.manyue.modules.video.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频评论DTO
 */
@Data
public class VideoCommentDTO {

    private Long id;
    private Long userId;
    private String nick;
    private String avatar;
    private Long videoId;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}