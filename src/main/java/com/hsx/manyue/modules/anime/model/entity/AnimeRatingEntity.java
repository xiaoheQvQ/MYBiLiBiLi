package com.hsx.manyue.modules.anime.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("anime_ratings")
public class AnimeRatingEntity extends BaseEntity<AnimeRatingEntity> {
    @TableField("series_id")
    private Long seriesId;
    
    @TableField("user_id")
    private Long userId;
    
    private BigDecimal score;
}