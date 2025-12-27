package com.hsx.manyue.modules.feed.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
public class PostCommentVO {
    private Long id;
    private UserSimpleVO user;
    private String content;
    private Integer likeCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
 
    // For replies
    private Long parentId;
    private UserSimpleVO replyToUser;
    private List<PostCommentVO> replies; // 可选，用于返回热门回复
    private Integer replyCount;
}