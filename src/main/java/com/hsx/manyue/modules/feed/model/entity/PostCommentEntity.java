package com.hsx.manyue.modules.feed.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
 
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_post_comment")
public class PostCommentEntity extends BaseEntity<PostCommentEntity> {
    private static final long serialVersionUID = 1L;
 
    private Long postId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private Integer likeCount;
}