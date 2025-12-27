package com.hsx.manyue.modules.video.model.dto;

import com.hsx.manyue.common.annotation.validation.UploadFile;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class ChangeVideoDTO {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private VideoAreaEnum area;  // 修改为枚举类型
    private List<TagDTO> tags;
    @UploadFile(max = 5, message = "视频文件不能超过 5 M")
    private MultipartFile coverFile;

}
