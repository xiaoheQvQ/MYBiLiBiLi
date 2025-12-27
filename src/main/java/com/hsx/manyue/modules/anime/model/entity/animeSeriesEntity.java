package com.hsx.manyue.modules.anime.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;



@Data
@TableName("anime_series")
public class animeSeriesEntity extends BaseEntity<animeSeriesEntity> {

    
    private String title;
    
    @TableField("cover_url")
    private String coverUrl;
    
    private String description;
    
    private String area;
    
    @TableField("season_number")
    private Integer seasonNumber;
    
    private Integer status;
    
    @TableField("user_id")
    private Long userId;

    @TableField("is_vip_only")
    private boolean isVipOnly;


    // 状态枚举
    public enum Status {
        UNPUBLISHED(0, "未发布"),
        PUBLISHED(1, "已发布"),
        OFFLINE(2, "下架");
        
        private final int code;
        private final String desc;
        
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}