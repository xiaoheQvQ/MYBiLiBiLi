package com.hsx.manyue.modules.anime.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnimeSeriesDTO {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private String area;
    private Integer seasonNumber;
    private Integer status;
    private Long userId;
    private List<String> tags;
    private List<AnimeEpisodeDTO> episodes;
    
    @Data
    public static class AnimeEpisodeDTO {
        private Long id;
        private Integer episodeNumber;
        private String title;
        private String description;
        private String videoUrl;
        private Integer duration;
        private Integer status;
        private int min_member_level;
    }
}