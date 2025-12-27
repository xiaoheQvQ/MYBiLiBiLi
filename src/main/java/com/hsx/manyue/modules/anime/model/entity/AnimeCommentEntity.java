package com.hsx.manyue.modules.anime.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("anime_comments")
public class AnimeCommentEntity extends BaseEntity<AnimeCommentEntity> {
    @TableField("series_id")
    private Long seriesId;
    
    @TableField("episode_id")
    private Long episodeId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("root_id")
    private Long rootId;
    
    private String content;
    
    @TableField("like_count")
    private Integer likeCount;
    
    @TableField("reply_count")
    private Integer replyCount;
}