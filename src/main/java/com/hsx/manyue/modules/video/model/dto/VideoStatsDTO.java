package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
public class VideoStatsDTO {
    // 日期
    private String date;
    // 视频数量
    private Integer videoCount;
    // 观看次数
    private Long viewCount;
    // 评论数
    private Integer commentCount;
    // 点赞数
    private Integer likeCount;
    // 收藏数
    private Integer collectCount;
    // 弹幕数
    private Integer danmakuCount;
}