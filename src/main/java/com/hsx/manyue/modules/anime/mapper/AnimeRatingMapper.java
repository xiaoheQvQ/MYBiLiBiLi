package com.hsx.manyue.modules.anime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.anime.model.entity.AnimeRatingEntity;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface AnimeRatingMapper extends BaseMapper<AnimeRatingEntity> {
    @Select("SELECT AVG(score) as averageScore, COUNT(*) as ratingCount FROM anime_ratings " +
            "WHERE series_id = #{seriesId} AND is_delete = 0")
    Map<String, Object> selectRatingStats(Long seriesId);
}