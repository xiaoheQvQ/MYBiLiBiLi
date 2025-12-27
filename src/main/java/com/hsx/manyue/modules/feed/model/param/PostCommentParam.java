package com.hsx.manyue.modules.feed.model.param;

import lombok.Data;
 
@Data
public class PostCommentParam {
    private String content;
    private Long parentId;
    private Long replyToUserId;
}