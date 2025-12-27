package com.hsx.manyue.modules.video.model.vo;

import com.hsx.manyue.common.enums.VideoStatusEnum;

import lombok.Data;

@Data

public class VideoSeriesVO {

    private Long seriesId;
    

    private Long videoId;
    

    private String title;

    private String partTitle;


    private String partDescription;
    

    private String cover;
    

    private Integer sortOrder;

    private Float duration;

    private Long viewCount;
    

    private VideoStatusEnum status;
}