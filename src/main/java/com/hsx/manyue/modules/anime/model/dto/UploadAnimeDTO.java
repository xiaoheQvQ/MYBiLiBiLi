package com.hsx.manyue.modules.anime.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class UploadAnimeDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "番剧标题不能为空")
    private String title;
    
    @NotBlank(message = "番剧描述不能为空")
    private String description;
    
    private String area;
    
    @NotNull(message = "季数不能为空")
    @Min(value = 1, message = "季数最小为1")
    private Integer seasonNumber;
    
    @NotNull(message = "封面文件不能为空")
    private MultipartFile coverFile;
    
    @NotEmpty(message = "标签不能为空")
    private List<String> tags;
    
    @NotEmpty(message = "分集信息不能为空")
    private List<EpisodeDTO> episodes;
    
    @Data
    public static class EpisodeDTO {
        @NotNull(message = "分集号不能为空")
        @Min(value = 1, message = "分集号最小为1")
        private Integer episodeNumber;
        
        @NotBlank(message = "分集标题不能为空")
        private String title;
        
        private String description;
        
        @NotNull(message = "视频文件不能为空")
        private MultipartFile videoFile;
        
        @NotNull(message = "视频时长不能为空")
        private Integer duration;
        
        @NotBlank(message = "视频MD5不能为空")
        private String md5;
    }
}