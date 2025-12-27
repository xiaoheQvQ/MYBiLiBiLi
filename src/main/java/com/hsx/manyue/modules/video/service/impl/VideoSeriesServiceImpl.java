package com.hsx.manyue.modules.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.video.mapper.VideoMapper;

import com.hsx.manyue.modules.video.mapper.VideoSeriesMapper;
import com.hsx.manyue.modules.video.model.dto.*;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.model.entity.VideoSeriesEntity;
import com.hsx.manyue.modules.video.service.IvideoSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class VideoSeriesServiceImpl extends ServiceImpl<VideoSeriesMapper, VideoSeriesEntity> implements IvideoSeriesService {

    @Autowired
    private VideoSeriesMapper videoSeriesMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public VideoSeriesListVO getVideoSeriesList(Long videoId) {
        VideoEntity mainVideo = videoMapper.selectById(videoId);
        if (mainVideo == null) {
            throw new IllegalArgumentException("视频不存在");
        }
        // 1. 先通过 videoId 查询对应的 seriesId
        VideoSeriesEntity videoSeries = this.lambdaQuery()
                .eq(VideoSeriesEntity::getVideoId, videoId)
                .one();

        if (videoSeries == null) {
            log.info("No series found for videoId: {}", videoId);
            return (VideoSeriesListVO) Collections.emptyList();
        }

        Long seriesId = videoSeries.getSeriesId();


        LambdaQueryWrapper<VideoSeriesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoSeriesEntity::getSeriesId, seriesId)
                .eq(VideoSeriesEntity::getIsDelete, false)
                .orderByAsc(VideoSeriesEntity::getSortOrder);

        List<VideoSeriesEntity> seriesEntities = videoSeriesMapper.selectList(queryWrapper);

        VideoSeriesListVO vo = new VideoSeriesListVO();
        vo.setMainVideo(new VideoSeriesMainVO(mainVideo,seriesId));

        List<VideoSeriesItemVO> seriesList = seriesEntities.stream()
                .map(entity -> new VideoSeriesItemVO(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getDescription(),
                        entity.getSortOrder()
                ))
                .collect(Collectors.toList());

        vo.setSeriesList(seriesList);
        return vo;
    }

    @Override
    public void addVideoSeries(AddVideoSeriesDTO dto) {
        // 验证主视频是否存在且属于当前用户
        VideoEntity mainVideo = videoMapper.selectById(dto.getVideoId());
        if (mainVideo == null || !mainVideo.getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("无权操作该视频");
        }

        // 获取当前最大排序号
        LambdaQueryWrapper<VideoSeriesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoSeriesEntity::getSeriesId, dto.getSeriesId())
                .select(VideoSeriesEntity::getSortOrder)
                .orderByDesc(VideoSeriesEntity::getSortOrder)
                .last("LIMIT 1");

        VideoSeriesEntity lastSeries = videoSeriesMapper.selectOne(queryWrapper);
        int sortOrder = lastSeries != null ? lastSeries.getSortOrder() + 1 : 1;

        VideoSeriesEntity entity = new VideoSeriesEntity();
        entity.setSeriesId(dto.getSeriesId());
        entity.setVideoId(dto.getVideoId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setSortOrder(sortOrder);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        videoSeriesMapper.insert(entity);
    }

    @Override
    public void updateVideoSeries(UpdateVideoSeriesDTO dto) {
        VideoSeriesEntity entity = videoSeriesMapper.selectById(dto.getId());
        if (entity == null) {
            throw new IllegalArgumentException("分P视频不存在");
        }

        // 验证用户权限
        VideoEntity video = videoMapper.selectById(entity.getVideoId());
        if (video == null || !video.getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("无权操作该视频");
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setUpdateTime(new Date());

        videoSeriesMapper.updateById(entity);
    }

    @Override
    public void deleteVideoSeries(Long id, Long userId) {
        VideoSeriesEntity entity = videoSeriesMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("分P视频不存在");
        }

        // 验证用户权限
        VideoEntity video = videoMapper.selectById(entity.getVideoId());
        if (video == null || !video.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该视频");
        }

        // 逻辑删除
        entity.setIsDelete(1);
        entity.setUpdateTime(new Date());
        videoSeriesMapper.updateById(entity);
    }

    @Override
    public void sortVideoSeries(SortVideoSeriesDTO dto) {

        System.out.println("dto:"+dto);
        // 批量更新排序
        List<VideoSeriesEntity> updateList = dto.getSortList().stream()
                .map(item -> {
                    VideoSeriesEntity entity = new VideoSeriesEntity();
                    entity.setId(item.getId());
                    entity.setSortOrder(item.getSortOrder());
                    entity.setUpdateTime(new Date());
                    return entity;
                })
                .collect(Collectors.toList());

        this.updateBatchById(updateList);
    }


}
