package com.hsx.manyue.modules.anime.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AnimeRatingDTO {
    private Long seriesId;
    private Long userId;
    private BigDecimal score;
    private BigDecimal averageScore;
    private Integer ratingCount;
}