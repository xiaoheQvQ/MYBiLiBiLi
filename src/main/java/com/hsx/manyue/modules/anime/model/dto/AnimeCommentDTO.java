package com.hsx.manyue.modules.anime.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnimeCommentDTO {
    private Long id;
    private Long seriesId;
    private Long episodeId;
    private Long userId;
    private String username;
    private String avatar;
    private Long parentId;
    private Long rootId;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private String createTime;
    private Boolean isLiked;
    private List<AnimeCommentDTO> replies;
}