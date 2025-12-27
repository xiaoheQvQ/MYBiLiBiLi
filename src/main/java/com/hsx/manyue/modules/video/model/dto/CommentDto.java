package com.hsx.manyue.modules.video.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDto {
    /**
     * 主键
     */
    private Long id;  // 改为Long类型与表结构一致

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 上级评论ID
     */
    private Long parentId;


    private UserDTO user;  // 评论用户信息

    /**
     * 评论内容
     */
    private String content;

    /**
     * 下级评论
     */
    private List<CommentDto> sons;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
