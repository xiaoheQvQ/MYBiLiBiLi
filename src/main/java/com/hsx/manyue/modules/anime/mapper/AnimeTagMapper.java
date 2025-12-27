package com.hsx.manyue.modules.anime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.anime.model.entity.animeTagEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
public interface AnimeTagMapper extends BaseMapper<animeTagEntity> {
    @Insert("<script>" +
            "INSERT INTO anime_tags (series_id, tag_name) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.seriesId}, #{item.tagName})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<animeTagEntity> tagList);

    @Select("SELECT series_id FROM anime_tags WHERE tag_name = #{tagName}")
    List<Long> selectSeriesIdsByTag(@Param("tagName") String tagName);
}