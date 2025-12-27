package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

@Data
public class SeriesVideoDTO {
    private Long seriesId;
    private String seriesTitle;
    private String partTitle;
    private String partDescription;
    private Integer sortOrder;
}
