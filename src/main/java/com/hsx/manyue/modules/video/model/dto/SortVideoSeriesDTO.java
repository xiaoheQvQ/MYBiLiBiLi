package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class SortVideoSeriesDTO {
    private Long userId;
    
    @NotNull(message = "系列ID不能为空")
    private Long seriesId;
    
    @NotEmpty(message = "排序列表不能为空")
    private List<SortItem> sortList;
    
    @Data
    public static class SortItem {
        @NotNull(message = "分PID不能为空")
        private Long id;
        
        @NotNull(message = "排序序号不能为空")
        private Integer sortOrder;
    }
}
