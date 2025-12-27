package com.hsx.manyue.modules.video.model.dto;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;

import lombok.Data;


@Data
public class VideoSeriesMainVO {
    private String title;
    
    private String area;
    
    private Long seriesId;

    public VideoSeriesMainVO(VideoEntity video,Long seriesId) {
        this.title = video.getTitle();
        this.area = video.getArea().getDetail();
        this.seriesId = seriesId;
    }
}
