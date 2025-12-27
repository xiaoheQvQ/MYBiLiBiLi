package com.hsx.manyue.modules.anime.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.anime.model.dto.AnimeSeriesDTO;
import com.hsx.manyue.modules.anime.model.dto.UploadAnimeDTO;
import com.hsx.manyue.modules.anime.model.entity.animeSeriesEntity;

import java.io.IOException;
import java.util.List;

public interface AnimeService extends IService<animeSeriesEntity> {
    Long uploadAnime(UploadAnimeDTO animeDTO) throws IOException;

    AnimeSeriesDTO.AnimeEpisodeDTO getEpisode(Long episodeId);

    List<AnimeSeriesDTO> listAnimeSeries(String title, String tag, String area, Integer page, Integer size);

    AnimeSeriesDTO getAnimeSeries(Long seriesId);
}