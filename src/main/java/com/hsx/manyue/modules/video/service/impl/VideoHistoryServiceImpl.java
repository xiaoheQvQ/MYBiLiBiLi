package com.hsx.manyue.modules.video.service.impl;

import eu.bitwalker.useragentutils.UserAgent;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.utils.IpUtil;
import com.hsx.manyue.modules.video.mapper.VideoHistoryMapper;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoHistoryEntity;
import com.hsx.manyue.modules.video.service.IVideoHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 视频观看历史记录 服务实现类
 * 优化：使用Redis批量缓存+定时刷新，减少数据库操作
 */
@Service
@RequiredArgsConstructor
public class VideoHistoryServiceImpl extends ServiceImpl<VideoHistoryMapper, VideoHistoryEntity> implements IVideoHistoryService {

    private final VideoHistoryBatchService batchService;

    /**
     * 更新观看历史（优化版）
     * 使用Redis缓存，避免每秒都执行数据库操作
     */
    @Override
    public void updateHistory(Long userId, Long videoId, Double time) {
        // 异步更新到Redis，定时批量刷新到数据库
        batchService.updateHistoryAsync(userId, videoId, time);
    }

    /**
     * 游客观看历史更新（优化版）
     * 使用Redis缓存，避免频繁数据库操作
     */
    @Override
    public void updateHistoryUnlogin(Long videoId, Double time, HttpServletRequest request) {
        //生成clientId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);

        // 异步更新到Redis，定时批量刷新到数据库
        batchService.updateHistoryUnloginAsync(videoId, time, clientId, ip);
    }

    @Override
    public Double getPlayPosition(Long userId, Long videoId) {
        VideoHistoryEntity history = this.lambdaQuery()
                .eq(VideoHistoryEntity::getUserId, userId)
                .eq(VideoHistoryEntity::getVideoId, videoId)
                .one();
        return history == null ? -1 : history.getTime();
    }

    @Override
    public List<VideoDTO> histories(Long userId) {
        List<VideoDTO> list = baseMapper.histories(userId);
        return list;
    }


}
