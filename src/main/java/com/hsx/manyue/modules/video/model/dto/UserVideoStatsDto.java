package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用户近七日视频数据统计
 */
@Data
public class UserVideoStatsDto {


    private LocalDate statDate;

    /**
     * 观看视频数量（去重）
     */
    private Integer viewCount;

    /**
     * 点赞视频数量（去重）
     */
    private Integer likeCount;

    /**
     * 收藏视频数量（去重）
     */
    private Integer collectCount;

    /**
     * 评论数量（不去重，按评论条数计算）
     */
    private Integer commentCount;

    /**
     * 弹幕数量（不去重，按弹幕条数计算）
     */
    private Integer danmakuCount;
}