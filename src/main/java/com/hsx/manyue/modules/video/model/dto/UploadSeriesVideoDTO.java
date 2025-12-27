package com.hsx.manyue.modules.video.model.dto;


import jakarta.validation.constraints.Max;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class UploadSeriesVideoDTO extends UploadVideoDTO {
    
    @NotBlank(message = "分P标题不能为空")
    @Length(min = 2, max = 50, message = "分P标题长度应在2-50之间")
    private String partTitle;
    
    @Length(max = 200, message = "分P描述文本太长，请控制在200字符内")
    private String partDescription;
    
    @Min(value = 0, message = "排序序号不能小于0")
    @Max(value = 100, message = "排序序号不能大于100")
    private Integer sortOrder = 0;
    
    private Long seriesId;
}