package com.hsx.manyue.modules.video.controller;

import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.service.IVideoHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * 视频历史记录控制器
 */
@Tag(name = "视频历史记录管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/video/history")
public class VideoHistoryRestController extends SuperController {

    private final IVideoHistoryService historyService;

    @Operation(summary = "获取视频历史记录")
    @PutMapping("/{videoId}/{time}")
    @Login
    public R updateHistory(@PathVariable Long videoId, @PathVariable Double time, HttpServletRequest request) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        historyService.updateHistory(userId, videoId, time);
        return R.success();
    }

    @Operation(summary = "保存无登录状态的历史记录")
    @PutMapping("/Unlogin/{videoId}/{time}")
    public R updateHistoryUnlogin(@PathVariable Long videoId, @PathVariable Double time, HttpServletRequest request) {
        historyService.updateHistoryUnlogin(videoId, time,request);
        return R.success();
    }

    @Operation(summary = "获取当前视频的播放位置")
    @GetMapping("/{videoId}")
    @Login
    public R getVideoPlayPosition(@PathVariable Long videoId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        Double time = historyService.getPlayPosition(userId, videoId);
        return R.success(time);
    }

    @Operation(summary = "获取用户的历史播放记录")
    @GetMapping
    @Login
    public R getHistory() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<VideoDTO> list = historyService.histories(userId);
        System.out.println("历史播放记录"+list.toString());
        return R.success(list);
    }
}
