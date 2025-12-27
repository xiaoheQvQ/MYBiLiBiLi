package com.hsx.manyue.modules.feed.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
 
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_post")
public class PostEntity extends BaseEntity<PostEntity> {
    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    private Long userId;
    private String content;
    private String location;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status; // 0-私密, 1-公开, 2-审核中, 3-已删除
}