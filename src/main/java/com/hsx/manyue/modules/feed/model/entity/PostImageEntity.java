package com.hsx.manyue.modules.feed.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
 
@Data
@TableName("t_post_image")
public class PostImageEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long postId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createTime;
}