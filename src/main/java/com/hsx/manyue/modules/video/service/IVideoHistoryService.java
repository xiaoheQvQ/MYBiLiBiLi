package com.hsx.manyue.modules.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoHistoryEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 视频观看历史记录 服务类
 */
public interface IVideoHistoryService extends IService<VideoHistoryEntity> {

    void updateHistory(Long userId, Long videoId, Double time);

    Double getPlayPosition(Long userId, Long videoId);

    List<VideoDTO> histories(Long userId);

    void updateHistoryUnlogin(Long videoId, Double time, HttpServletRequest request);
}
