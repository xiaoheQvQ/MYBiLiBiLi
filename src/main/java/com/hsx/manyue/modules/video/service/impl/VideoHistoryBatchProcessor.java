package com.hsx.manyue.modules.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hsx.manyue.common.config.BufferTriggerConfig;
import com.hsx.manyue.modules.video.mapper.VideoHistoryMapper;
import com.hsx.manyue.modules.video.model.entity.VideoHistoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 视频观看历史批量处理器
 * 职责：专门处理 BufferTrigger 收集的观看历史数据的批量保存
 * 
 * 设计模式：策略模式 (Strategy Pattern)
 * - 将批量处理逻辑从配置类中分离出来，符合单一职责原则
 * - 解决循环依赖：BufferTriggerConfig 不再直接依赖 IVideoHistoryService
 * 
 * @author 何世兴
 * @since 2025-12-28
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoHistoryBatchProcessor {

    private final VideoHistoryMapper videoHistoryMapper;

    /**
     * 批量保存视频观看历史
     * 
     * 业务逻辑：
     * 1. 遍历 BufferTrigger 收集的所有观看记录
     * 2. 区分登录用户和游客记录
     * 3. 对于登录用户：查询是否存在记录，存在则更新，不存在则新增
     * 4. 对于游客：查询是否存在记录，存在则更新，不存在则新增
     * 5. 记录批量处理结果日志
     */
    public void batchSaveVideoHistory(Map<BufferTriggerConfig.VideoHistoryKey, BufferTriggerConfig.VideoHistoryData> buffer) {
        if (buffer.isEmpty()) {
            return;
        }

        try {
            int updateCount = 0;
            for (Map.Entry<BufferTriggerConfig.VideoHistoryKey, BufferTriggerConfig.VideoHistoryData> entry : buffer.entrySet()) {
                BufferTriggerConfig.VideoHistoryKey key = entry.getKey();
                BufferTriggerConfig.VideoHistoryData data = entry.getValue();

                VideoHistoryEntity entity;
                if (key.isGuest()) {
                    // 游客记录：先查询是否存在
                    entity = videoHistoryMapper.selectOne(new LambdaQueryWrapper<VideoHistoryEntity>()
                            .eq(VideoHistoryEntity::getClientId, key.getClientId())
                            .eq(VideoHistoryEntity::getVideoId, key.getVideoId())
                            .eq(VideoHistoryEntity::getIp, key.getIp()));
                    
                    if (entity == null) {
                        entity = new VideoHistoryEntity()
                                .setClientId(key.getClientId())
                                .setVideoId(key.getVideoId())
                                .setIp(key.getIp());
                        entity.setTime(data.getTime());
                        videoHistoryMapper.insert(entity);
                    } else {
                        entity.setTime(data.getTime());
                        videoHistoryMapper.updateById(entity);
                    }
                } else {
                    // 登录用户记录：先查询是否存在
                    entity = videoHistoryMapper.selectOne(new LambdaQueryWrapper<VideoHistoryEntity>()
                            .eq(VideoHistoryEntity::getUserId, key.getUserId())
                            .eq(VideoHistoryEntity::getVideoId, key.getVideoId()));

                    if (entity == null) {
                        entity = new VideoHistoryEntity()
                                .setUserId(key.getUserId())
                                .setVideoId(key.getVideoId());
                        entity.setTime(data.getTime());
                        videoHistoryMapper.insert(entity);
                    } else {
                        entity.setTime(data.getTime());
                        videoHistoryMapper.updateById(entity);
                    }
                }
                updateCount++;
            }

            log.info("BufferTrigger 批量更新观看历史记录成功，共更新 {} 条记录", updateCount);
        } catch (Exception e) {
            log.error("BufferTrigger 批量刷新观看历史记录失败", e);
        }
    }
}
