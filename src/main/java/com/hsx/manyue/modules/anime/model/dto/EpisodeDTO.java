package com.hsx.manyue.modules.anime.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

@Data
public class EpisodeDTO {
    /**
     * 分集号
     */
    @NotNull(message = "分集号不能为空")
    @Min(value = 1, message = "分集号最小为1")
    private Integer episodeNumber;
    
    /**
     * 分集标题
     */
    @NotBlank(message = "分集标题不能为空")
    @Size(max = 100, message = "分集标题长度不能超过100个字符")
    private String title;
    
    /**
     * 分集描述
     */
    @Size(max = 500, message = "分集描述长度不能超过500个字符")
    private String description;
    
    /**
     * 视频文件
     */
    @NotNull(message = "视频文件不能为空")
    private MultipartFile videoFile;
    
    /**
     * 视频时长(秒)
     */
    @NotNull(message = "视频时长不能为空")
    @Min(value = 1, message = "视频时长最小为1秒")
    @Max(value = 3600, message = "视频时长最大为3600秒(1小时)")
    private Integer duration;
    
    /**
     * 视频文件MD5
     */
    @NotBlank(message = "视频MD5不能为空")
    @Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "MD5格式不正确")
    private String md5;
    
    /**
     * 视频文件大小(字节)
     * 前端计算后传入，用于校验
     */
    @NotNull(message = "视频文件大小不能为空")
    @Min(value = 1, message = "视频文件大小不能为0")
    private Long fileSize;
}