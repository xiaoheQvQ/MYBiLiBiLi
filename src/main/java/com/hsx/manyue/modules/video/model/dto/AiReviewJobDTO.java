package com.hsx.manyue.modules.video.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data

@AllArgsConstructor
public class AiReviewJobDTO {


    private String jobId;

    private Long videoId;

    private String aliyunVideoId;
}
