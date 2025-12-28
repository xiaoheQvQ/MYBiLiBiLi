package com.hsx.manyue.modules.video.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import com.hsx.manyue.common.enums.VideoStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema( name= "视频信息")

public class VideoDTO {

    @Schema(description = "视频ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nick;

    @Schema
    private String avatar;

    @Schema(description = "视频标题")
    private String title;
    @Schema(description = "视频字幕")
    private String subtitle;

    @Schema(description = "视频描述")
    private String description;

    @Schema(description = "视频时长")
    private Float duration;

    @Schema(description = "阿里云视频ID")
    private String videoId;

    @Schema(description = "视频播放url")
    private String playUrl;

    @Schema(description = "封面url")
    private String cover;

    @Schema(description = "视频md5")
    private String md5;

    @Schema(description = "点赞数量")
    private Long like;

    @Schema(description = "收藏数量")
    private Integer collect;

    @Schema(description = "观看次数")
    private Integer count;

    @Schema(description = "弹幕数量")
    private Long danmakus;

    @Schema(description = "视频分区")

    private VideoAreaEnum area;

    @Schema(description = "视频状态")
    private VideoStatusEnum status;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    private LocalDateTime createTime;

    @Schema(description = "历史记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime viewTime;

    @Schema(description = "观看进度")
    private Integer progress;

    @Schema(description = "标签")

    private List<TagDTO> tags;

    @Schema(description = "标签-字符串形式")
    private String tagsStr;

    @Schema(description = "粉丝数量")
    private Long followerCount;

    @Schema(description = "关注数量")
    private Long followingCount;
    
    @Schema(description = "ES搜索评分（用于调试和排序）")
    private Double esScore;
}
