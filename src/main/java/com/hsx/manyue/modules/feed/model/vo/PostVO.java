package com.hsx.manyue.modules.feed.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
public class PostVO {
    private Long id;
    private UserSimpleVO author;
    private String content;
    private String location;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked; // 当前用户是否已点赞
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}