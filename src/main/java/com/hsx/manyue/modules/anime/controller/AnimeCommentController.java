package com.hsx.manyue.modules.anime.controller;

import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.anime.model.dto.AnimeCommentDTO;
import com.hsx.manyue.modules.anime.model.dto.AnimeRatingDTO;
import com.hsx.manyue.modules.anime.model.service.AnimeCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/anime/comment")
public class AnimeCommentController {
    private final AnimeCommentService animeCommentService;

    // 添加评论
    @PostMapping("/add")
    public R addComment(@RequestBody AnimeCommentDTO commentDTO) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        commentDTO.setUserId(userId);
        return R.success(animeCommentService.addComment(commentDTO));
    }

    // 获取评论列表
    @GetMapping("/list")
    public R listComments(
            @RequestParam Long seriesId,
            @RequestParam(required = false) Long episodeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        System.out.println("seriesId"+seriesId);
        System.out.println("episodeId"+episodeId);
        List<AnimeCommentDTO> comments = animeCommentService.listComments(seriesId, episodeId, page, size);
        System.out.println("comments"+comments);
        return R.success(comments);
    }

    // 点赞评论
    @PostMapping("/like/{commentId}")
    public R likeComment(@PathVariable Long commentId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(animeCommentService.likeComment(commentId, userId));
    }

    // 取消点赞评论
    @PostMapping("/unlike/{commentId}")
    public R unlikeComment(@PathVariable Long commentId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(animeCommentService.unlikeComment(commentId, userId));
    }

    // 添加评分
    @PostMapping("/rating/add")
    public R addRating(@RequestBody AnimeRatingDTO ratingDTO) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        ratingDTO.setUserId(userId);
        return R.success(animeCommentService.addRating(ratingDTO));
    }

    // 获取评分信息
    @GetMapping("/rating/info")
    public R getRatingInfo(@RequestParam Long seriesId) {
        AnimeRatingDTO ratingInfo = animeCommentService.getRatingInfo(seriesId);
        return R.success(ratingInfo);
    }

    // 获取用户评分
    @GetMapping("/rating/user")
    public R getUserRating(@RequestParam Long seriesId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        AnimeRatingDTO userRating = animeCommentService.getUserRating(seriesId, userId);
        return R.success(userRating);
    }
}