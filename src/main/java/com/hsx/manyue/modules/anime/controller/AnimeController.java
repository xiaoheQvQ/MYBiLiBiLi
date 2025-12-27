package com.hsx.manyue.modules.anime.controller;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.anime.model.dto.AnimeCommentDTO;
import com.hsx.manyue.modules.anime.model.dto.AnimeRatingDTO;
import com.hsx.manyue.modules.anime.model.dto.AnimeSeriesDTO;
import com.hsx.manyue.modules.anime.model.dto.UploadAnimeDTO;
import com.hsx.manyue.modules.anime.model.service.AnimeCommentService;
import com.hsx.manyue.modules.anime.model.service.AnimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/anime")
public class AnimeController {
    private final AnimeService animeService;

    @PostMapping("/upload")
    public R uploadAnime(UploadAnimeDTO animeDTO,
                         MultipartFile coverFile) throws IOException {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        animeDTO.setUserId(userId);
        animeDTO.setCoverFile(coverFile);
        Long seriesId = animeService.uploadAnime(animeDTO);
        return R.success(seriesId);
    }

    @GetMapping("/{seriesId}")
    public R getAnimeSeries(@PathVariable Long seriesId) {
        AnimeSeriesDTO animeSeries = animeService.getAnimeSeries(seriesId);
        return R.success(animeSeries);
    }

    @GetMapping("/list")
    public R listAnimeSeries(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String area,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<AnimeSeriesDTO> list = animeService.listAnimeSeries(title, tag, area, page, size);
        return R.success(list);
    }

    @GetMapping("/episode/{episodeId}")
    public R getEpisode(@PathVariable Long episodeId) {
        AnimeSeriesDTO.AnimeEpisodeDTO episode = animeService.getEpisode(episodeId);
        return R.success(episode);
    }



}