package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;


import java.util.List;

@Data
public class VideoSeriesListVO {
    private VideoSeriesMainVO mainVideo;
    
    private List<VideoSeriesItemVO> seriesList;
}