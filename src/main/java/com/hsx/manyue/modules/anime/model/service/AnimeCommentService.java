package com.hsx.manyue.modules.anime.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.anime.model.dto.AnimeCommentDTO;
import com.hsx.manyue.modules.anime.model.dto.AnimeRatingDTO;
import com.hsx.manyue.modules.anime.model.entity.AnimeCommentEntity;

import java.util.List;

public interface AnimeCommentService extends IService<AnimeCommentEntity> {
    AnimeCommentDTO addComment(AnimeCommentDTO commentDTO);
    
    List<AnimeCommentDTO> listComments(Long seriesId, Long episodeId, Integer page, Integer size);
    
    boolean likeComment(Long commentId, Long userId);
    
    boolean unlikeComment(Long commentId, Long userId);
    
    AnimeRatingDTO addRating(AnimeRatingDTO ratingDTO);
    
    AnimeRatingDTO getRatingInfo(Long seriesId);
    
    AnimeRatingDTO getUserRating(Long seriesId, Long userId);
}