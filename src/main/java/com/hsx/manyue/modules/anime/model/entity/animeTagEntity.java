package com.hsx.manyue.modules.anime.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;


@Data
@TableName("anime_tags")
public class animeTagEntity  extends BaseEntity<animeTagEntity> {

    
    @TableField("series_id")
    private Long seriesId;
    
    @TableField("tag_name")
    private String tagName;

}