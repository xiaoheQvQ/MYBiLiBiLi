package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
@Data
public class UpdateVideoSeriesDTO {

    private Long userId;

    @NotNull(message = "分PID不能为空")
    private Long id;

    @NotBlank(message = "分P标题不能为空")
    @Size(min = 2, max = 50, message = "分P标题长度必须在2-50个字符之间")
    private String title;

    private String description;
}