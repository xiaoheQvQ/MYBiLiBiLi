package com.hsx.manyue.modules.feed.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_post_like")
public class PostLikeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createTime;
}