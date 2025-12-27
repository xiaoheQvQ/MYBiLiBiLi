package com.hsx.manyue.modules.anime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.anime.model.entity.AnimeEpisodeMemberAccessEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AnimeEpisodeMemberAccessMapper extends BaseMapper<AnimeEpisodeMemberAccessEntity> {
    
    @Select("SELECT * FROM anime_episode_member_access WHERE episode_id = #{episodeId} AND is_delete = 0")
    AnimeEpisodeMemberAccessEntity selectByEpisodeId(@Param("episodeId") Long episodeId);
}