package com.hsx.manyue.modules.anime.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;


@Data
@TableName("anime_episodes")
public class animeEpisodeEntity extends BaseEntity<animeEpisodeEntity> {

    
    @TableField("series_id")
    private Long seriesId;
    
    @TableField("episode_number")
    private Integer episodeNumber;
    
    private String title;
    
    private String description;
    
    @TableField("video_url")
    private String videoUrl;
    
    private Integer duration;
    
    private Integer status;
    
    private String md5;
    


    
    // 状态枚举
    public enum Status {
        UPLOADING(0, "上传中"),
        TRANSCODING(1, "转码中"),
        PUBLISHED(2, "发布成功"),
        TRANSCODE_FAILED(3, "转码失败");
        
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