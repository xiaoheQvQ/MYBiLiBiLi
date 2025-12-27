package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;


@Data
public class ownVideoStatsDTO {

    private Long videoId;
    private String videoTitle;
    private LocalDate statDate;
    private Integer likeCount;
    private Integer collectionCount;
    private Integer danmakuCount;
    private Integer viewCount;
    private Integer commentCount;


}
