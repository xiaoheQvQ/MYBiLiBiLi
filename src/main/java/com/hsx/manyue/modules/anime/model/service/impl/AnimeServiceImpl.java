package com.hsx.manyue.modules.anime.model.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.anime.mapper.AnimeEpisodeMapper;
import com.hsx.manyue.modules.anime.mapper.AnimeEpisodeMemberAccessMapper;
import com.hsx.manyue.modules.anime.mapper.AnimeMapper;
import com.hsx.manyue.modules.anime.mapper.AnimeTagMapper;
import com.hsx.manyue.modules.anime.model.dto.AnimeSeriesDTO;
import com.hsx.manyue.modules.anime.model.dto.UploadAnimeDTO;
import com.hsx.manyue.modules.anime.model.entity.AnimeEpisodeMemberAccessEntity;
import com.hsx.manyue.modules.anime.model.entity.animeSeriesEntity;
import com.hsx.manyue.modules.anime.model.entity.animeTagEntity;
import com.hsx.manyue.modules.anime.model.dto.EpisodeDTO;
import com.hsx.manyue.modules.anime.model.entity.animeEpisodeEntity;
import com.hsx.manyue.modules.anime.model.service.AnimeService;
import com.hsx.manyue.modules.oss.service.impl.OssServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.hsx.manyue.common.constant.RedisKeys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class AnimeServiceImpl extends ServiceImpl<AnimeMapper, animeSeriesEntity> implements AnimeService {
    private final AnimeEpisodeMapper episodeMapper;
    private final AnimeTagMapper tagMapper;
    private final OssServiceImpl ossService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AnimeEpisodeMemberAccessMapper accessMapper;

    private final ThreadPoolExecutor videoUploadExecutor = new ThreadPoolExecutor(
            3, 10,
            5, TimeUnit.MINUTES, new LinkedBlockingDeque<>(),
            ThreadUtil.createThreadFactory("upload-video-executor")
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadAnime(UploadAnimeDTO animeDTO) throws IOException {
        // 上传封面
        String coverUrl = ossService.uploadFile(animeDTO.getCoverFile());

        // 保存番剧基本信息
        animeSeriesEntity series = new animeSeriesEntity();
        Long seriesId = IdUtil.getSnowflakeNextId();

        series.setId(seriesId);
        series.setTitle(animeDTO.getTitle());
        series.setDescription(animeDTO.getDescription());
        series.setArea(animeDTO.getArea());
        series.setSeasonNumber(animeDTO.getSeasonNumber());
        series.setCoverUrl(coverUrl);
        series.setUserId(animeDTO.getUserId());
        series.setStatus(1); // 未发布

        this.save(series);

        // 保存标签
        saveTags(seriesId, animeDTO.getTags());

        // 保存分集信息
        saveEpisodes(seriesId, animeDTO.getEpisodes());

        // 更新上传次数
        updateUploadCount(animeDTO.getUserId());

        return seriesId;
    }

    private void saveTags(Long seriesId, List<String> tags) {
        List<animeTagEntity> tagList = tags.stream()
                .map(tag -> {
                    animeTagEntity animeTag = new animeTagEntity();
                    animeTag.setSeriesId(seriesId);
                    animeTag.setTagName(tag);
                    return animeTag;
                })
                .collect(Collectors.toList());

        tagMapper.insertBatch(tagList);
    }

    private void saveEpisodes(Long seriesId, List<UploadAnimeDTO.EpisodeDTO> episodes) throws IOException {
        for (UploadAnimeDTO.EpisodeDTO episodeDTO : episodes) {
            animeEpisodeEntity episode = new animeEpisodeEntity();
            Long episodeId = IdUtil.getSnowflakeNextId();

            episode.setId(episodeId);
            episode.setSeriesId(seriesId);
            episode.setEpisodeNumber(episodeDTO.getEpisodeNumber());
            episode.setTitle(episodeDTO.getTitle());
            episode.setDescription(episodeDTO.getDescription());
            episode.setDuration(episodeDTO.getDuration());
            episode.setMd5(episodeDTO.getMd5());
            episode.setStatus(0); // 上传中

            episodeMapper.insert(episode);

            // 在事务提交前创建临时文件
            File tempVideoFile = createTempVideoFile(episodeDTO.getVideoFile());

            // 在事务提交后异步处理视频上传
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    videoUploadExecutor.execute(() -> {
                        try {
                            processVideoUpload(episodeId, tempVideoFile);
                        } finally {
                            // 确保删除临时文件
                            if (!tempVideoFile.delete()) {
                                log.warn("Failed to delete temp video file: {}", tempVideoFile.getAbsolutePath());
                            }
                        }
                    });
                }
            });
        }
    }

    private File createTempVideoFile(MultipartFile videoFile) throws IOException {
        String originalFilename = videoFile.getOriginalFilename();
        String subFix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        Path tempFilePath = Files.createTempFile("video-", subFix);
        File tempFile = tempFilePath.toFile();
        videoFile.transferTo(tempFile);
        return tempFile;
    }

    private void updateUploadCount(Long userId) {
        String uploadKey = RedisKeys.ANIME_UPLOAD + userId;
        redisTemplate.opsForValue().increment(uploadKey);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ZonedDateTime zonedDateTime = tomorrow.atStartOfDay(ZoneId.systemDefault());
        Date expired = Date.from(zonedDateTime.toInstant());
        redisTemplate.expireAt(uploadKey, expired);
    }

    @Async
    public void processVideoUpload(Long episodeId, File videoFile) {
        try {
            animeEpisodeEntity episode = episodeMapper.selectById(episodeId);
            if (episode == null) {
                log.error("分集不存在: {}", episodeId);
                return;
            }

            // 更新状态为转码中
            episode.setStatus(1);
            episodeMapper.updateById(episode);

            // 上传视频到OSS
            String videoUrl = ossService.uploadFile(videoFile);

            // 更新视频URL和状态
            episode.setVideoUrl(videoUrl);
            episode.setStatus(2); // 发布成功
            episodeMapper.updateById(episode);

        } catch (Exception e) {
            log.error("视频上传处理失败: {}", episodeId, e);
            // 更新状态为转码失败
            animeEpisodeEntity episode = new animeEpisodeEntity();
            episode.setId(episodeId);
            episode.setStatus(3);
            episodeMapper.updateById(episode);
        }
    }


    @Override
    public AnimeSeriesDTO getAnimeSeries(Long seriesId) {
        // 获取番剧基本信息
        animeSeriesEntity series = this.getById(seriesId);
        if (series == null) {
            throw new RuntimeException("番剧不存在");
        }

        // 获取标签
        List<String> tags = tagMapper.selectList(new LambdaQueryWrapper<animeTagEntity>()
                .eq(animeTagEntity::getSeriesId, seriesId)
                .select(animeTagEntity::getTagName)
        ).stream().map(animeTagEntity::getTagName).collect(Collectors.toList());

        // 获取分集信息（修正stream位置）
        List<AnimeSeriesDTO.AnimeEpisodeDTO> episodes = episodeMapper.selectList(
                        new LambdaQueryWrapper<animeEpisodeEntity>()
                                .eq(animeEpisodeEntity::getSeriesId, seriesId)
                ).stream()  // 在selectList的结果上调用stream
                .map(episode -> {
                    AnimeSeriesDTO.AnimeEpisodeDTO dto = new AnimeSeriesDTO.AnimeEpisodeDTO();
                    AnimeEpisodeMemberAccessEntity animeEpisodeMemberAccessEntity = accessMapper.selectByEpisodeId(episode.getId());
                    dto.setId(episode.getId());
                    dto.setEpisodeNumber(episode.getEpisodeNumber());
                    dto.setTitle(episode.getTitle());
                    dto.setDescription(episode.getDescription());
                    dto.setVideoUrl(episode.getVideoUrl());
                    dto.setDuration(episode.getDuration());
                    dto.setStatus(episode.getStatus());
                    if (animeEpisodeMemberAccessEntity != null) {dto.setMin_member_level(animeEpisodeMemberAccessEntity.getMinMemberLevel());}
                    else {
                        dto.setMin_member_level(0);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // 组装DTO
        AnimeSeriesDTO dto = new AnimeSeriesDTO();
        dto.setId(series.getId());
        dto.setTitle(series.getTitle());
        dto.setCoverUrl(series.getCoverUrl());
        dto.setDescription(series.getDescription());
        dto.setArea(series.getArea());
        dto.setSeasonNumber(series.getSeasonNumber());
        dto.setStatus(series.getStatus());
        dto.setUserId(series.getUserId());
        dto.setTags(tags);
        dto.setEpisodes(episodes);

        return dto;
    }

    @Override
    public List<AnimeSeriesDTO> listAnimeSeries(String title, String tag, String area, Integer page, Integer size) {
        System.out.println("tag:" + tag + " area:" + area); // 调试日志

        // 构建查询条件
        LambdaQueryWrapper<animeSeriesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(animeSeriesEntity::getStatus, animeSeriesEntity.Status.PUBLISHED.getCode());

        // 1. 先处理标签筛选（需要子查询）
        if (StringUtils.isNotBlank(tag)) {
            // 使用子查询找出有该标签的番剧ID
            queryWrapper.inSql(animeSeriesEntity::getId,
                    "SELECT DISTINCT series_id FROM anime_tags WHERE tag_name = '" + tag + "' AND is_delete = 0");
        }

        // 2. 其他筛选条件
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(animeSeriesEntity::getTitle, title);
        }
        if (StringUtils.isNotBlank(area)) {
            queryWrapper.eq(animeSeriesEntity::getArea, area);
        }

        // 分页查询
        Page<animeSeriesEntity> pageInfo = new Page<>(page, size);
        this.page(pageInfo, queryWrapper);

        // 转换为DTO（同时查询标签）
        return pageInfo.getRecords().stream().map(series -> {
            AnimeSeriesDTO dto = new AnimeSeriesDTO();
            dto.setId(series.getId());
            dto.setTitle(series.getTitle());
            dto.setCoverUrl(series.getCoverUrl());
            dto.setDescription(series.getDescription());
            dto.setArea(series.getArea());
            dto.setSeasonNumber(series.getSeasonNumber());
            dto.setStatus(series.getStatus());
            dto.setUserId(series.getUserId());

            // 查询标签（保持原有逻辑）
            List<String> tags = tagMapper.selectList(new LambdaQueryWrapper<animeTagEntity>()
                    .eq(animeTagEntity::getSeriesId, series.getId())
                    .select(animeTagEntity::getTagName)
            ).stream().map(animeTagEntity::getTagName).collect(Collectors.toList());
            dto.setTags(tags);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public AnimeSeriesDTO.AnimeEpisodeDTO getEpisode(Long episodeId) {
        animeEpisodeEntity episode = episodeMapper.selectById(episodeId);
        if (episode == null) {
            throw new RuntimeException("分集不存在");
        }
        AnimeSeriesDTO.AnimeEpisodeDTO dto = new AnimeSeriesDTO.AnimeEpisodeDTO();
        dto.setId(episode.getId());
        dto.setEpisodeNumber(episode.getEpisodeNumber());
        dto.setTitle(episode.getTitle());
        dto.setDescription(episode.getDescription());
        dto.setVideoUrl(episode.getVideoUrl());
        dto.setDuration(episode.getDuration());
        dto.setStatus(episode.getStatus());
        return dto;
    }

}