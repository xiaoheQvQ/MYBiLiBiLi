package com.hsx.manyue.modules.video.service.impl;

import eu.bitwalker.useragentutils.UserAgent;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.utils.IpUtil;
import com.hsx.manyue.modules.video.mapper.VideoHistoryMapper;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoHistoryEntity;
import com.hsx.manyue.modules.video.service.IVideoHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 视频观看历史记录 服务实现类
 */
@Service
public class VideoHistoryServiceImpl extends ServiceImpl<VideoHistoryMapper, VideoHistoryEntity> implements IVideoHistoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHistory(Long userId, Long videoId, Double time) {
        VideoHistoryEntity videoHistoryEntity = this.lambdaQuery()
                .eq(VideoHistoryEntity::getUserId, userId)
                .eq(VideoHistoryEntity::getVideoId, videoId)
                .oneOpt()
                .orElseGet(() -> new VideoHistoryEntity().setUserId(userId).setVideoId(videoId))
                .setTime(time);

        this.saveOrUpdate(videoHistoryEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHistoryUnlogin(Long videoId, Double time, HttpServletRequest request) {
        //生成clientId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);

        // 查询或创建观看记录
        VideoHistoryEntity videoHistoryEntity = this.lambdaQuery()
                .eq(VideoHistoryEntity::getClientId, clientId)
                .eq(VideoHistoryEntity::getVideoId, videoId)
                .eq(VideoHistoryEntity::getIp, ip)
                .oneOpt()
                .orElseGet(() -> new VideoHistoryEntity()
                        .setClientId(clientId)
                        .setVideoId(videoId)
                        .setIp(ip))
                .setTime(time);
        this.saveOrUpdate(videoHistoryEntity);
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
