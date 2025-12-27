package com.hsx.manyue.modules.video.controller;

import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.video.model.dto.AddVideoSeriesDTO;
import com.hsx.manyue.modules.video.model.dto.SortVideoSeriesDTO;
import com.hsx.manyue.modules.video.model.dto.UpdateVideoSeriesDTO;
import com.hsx.manyue.modules.video.model.dto.VideoSeriesListVO;
import com.hsx.manyue.modules.video.service.IvideoSeriesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/video/series")
public class VideoSeriesController {

    @Autowired
    private IvideoSeriesService videoSeriesService;

    @GetMapping("/list")
    public R getVideoSeriesList(@RequestParam Long videoId) {
        VideoSeriesListVO seriesList = videoSeriesService.getVideoSeriesList(videoId);
        return R.success(seriesList);
    }

    @PostMapping("/add")
    @Login
    public R addVideoSeries(@RequestBody @Valid AddVideoSeriesDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setUserId(userId);
        videoSeriesService.addVideoSeries(dto);
        return R.success();
    }

    @PostMapping("/update")
    @Login
    public R updateVideoSeries(@RequestBody @Valid UpdateVideoSeriesDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setUserId(userId);
        videoSeriesService.updateVideoSeries(dto);
        return R.success();
    }

    @PostMapping("/delete")
    @Login
    public R deleteVideoSeries(@RequestParam Long id) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        videoSeriesService.deleteVideoSeries(id, userId);
        return R.success();
    }

    @PostMapping("/sort")
    @Login
    public R sortVideoSeries(@RequestBody @Valid SortVideoSeriesDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setUserId(userId);
        System.out.println("排序"+dto);
        videoSeriesService.sortVideoSeries(dto);
        return R.success();
    }
}