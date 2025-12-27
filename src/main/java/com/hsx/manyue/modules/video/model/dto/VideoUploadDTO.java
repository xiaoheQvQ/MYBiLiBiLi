package com.hsx.manyue.modules.video.model.dto;

import com.hsx.manyue.modules.video.model.entity.TagEntity;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class VideoUploadDTO {
    private MultipartFile videoFile;
    private MultipartFile coverFile;
    private String title;
    private String description;
    private String area;
    private List<TagEntity> tags;
    private Long userId;
    private String md5;
}
