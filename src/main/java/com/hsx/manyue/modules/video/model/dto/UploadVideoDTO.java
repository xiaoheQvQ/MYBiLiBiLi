package com.hsx.manyue.modules.video.model.dto;

import com.hsx.manyue.common.annotation.validation.UploadFile;
import com.hsx.manyue.common.enums.VideoAreaEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;


@Data
public class UploadVideoDTO {


    private String videoId;


    @UploadFile(max = 500, message = "视频文件不能超过 500 M")
    private MultipartFile videoFile;


    @UploadFile(max = 5, message = "视频文件不能超过 5 M")
    private MultipartFile coverFile;


    private String cover;


    @NotNull(message = "用户信息不能为空")
    private Long userId;


    @NotBlank(message = "视频标题不能为空")
    @Length(min = 4, max = 20, message = "视频标题长度应在4-20之间")
    private String title;


    @Length(max = 500, message = "视频描述文本太长，请控制在500字符内")
    private String description;


    @NotBlank(message = "视频数据缺失")
    private String md5;


    @NotNull(message = "请选择视频分区")
    private VideoAreaEnum area;


    private List<TagDTO> tags;


    private Float duration;
}
