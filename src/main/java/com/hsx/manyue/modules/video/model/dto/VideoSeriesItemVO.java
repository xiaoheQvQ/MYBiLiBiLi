package com.hsx.manyue.modules.video.model.dto;


import lombok.Data;

@Data
public class VideoSeriesItemVO {

    private Long id;
    

    private String title;

    private String description;
    

    private Integer sortOrder;
    
    public VideoSeriesItemVO(Long id, String title, String description, Integer sortOrder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sortOrder = sortOrder;
    }
}