package com.hsx.manyue.modules.video.service.impl;
<<<<<<< HEAD
import com.hsx.manyue.modules.video.mapper.*;
import com.hsx.manyue.modules.video.model.entity.*;
import com.hsx.manyue.modules.video.model.vo.VideoSeriesVO;
=======
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hsx.manyue.modules.danmaku.mapper.DanmakuMapper;
import com.hsx.manyue.modules.video.mapper.*;
import com.hsx.manyue.modules.video.model.entity.*;
import com.hsx.manyue.modules.video.model.vo.VideoSeriesVO;
import com.hsx.manyue.modules.video.service.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
<<<<<<< HEAD
=======
import cn.hutool.json.JSONObject;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import cn.hutool.json.JSONUtil;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
<<<<<<< HEAD
=======
import com.hsx.manyue.common.config.MQConfig;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.enums.ReturnCodeEnums;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import com.hsx.manyue.common.enums.VideoStatusEnum;
import com.hsx.manyue.common.exception.ApiException;
import com.hsx.manyue.common.model.entity.BaseEntity;
<<<<<<< HEAD
=======
import com.hsx.manyue.common.utils.IpUtil;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.PageUtils;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;
import com.hsx.manyue.modules.danmaku.service.IDanmakuService;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.oss.service.IOssService;
import com.hsx.manyue.modules.oss.service.IVodService;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.user.service.IUserService;
import com.hsx.manyue.modules.user.service.IUserSubscriptionService;
import com.hsx.manyue.modules.video.model.dto.*;
import com.hsx.manyue.modules.video.model.param.AdminVideoQueryParam;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
<<<<<<< HEAD
import com.hsx.manyue.modules.video.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
=======

import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
<<<<<<< HEAD
=======
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
<<<<<<< HEAD
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.context.annotation.Lazy;
=======
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import org.springframework.context.annotation.Lazy;

>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
<<<<<<< HEAD
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
=======

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
<<<<<<< HEAD
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
=======

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
/**
 * 视频表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class VideoServiceImpl extends ServiceImpl<VideoMapper, VideoEntity> implements IVideoService {

    private final IVodService vodService;
    private final IOssService ossService;
    private final IDanmakuService danmakuService;
    private final RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedisUtil redisUtil;

    @Lazy
    private final IVideoLikeService videoLikeService;
    private final IvideoSeriesService videoSeriesService;
    private final IVideoCollectionService videoCollectionService;
    private final IVideoTagService videoTagService;
    private final IUserSubscriptionService userSubscriptionService;
    private final WebSocketServer webSocketServer;
    private final IUserService userService;
<<<<<<< HEAD
    
    // 推荐策略工厂 - 使用策略模式重构推荐逻辑
    private final com.hsx.manyue.modules.video.recommendation.VideoRecommendationStrategyFactory recommendationStrategyFactory;
=======
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3


    private final ThreadPoolExecutor videoUploadExecutor = new ThreadPoolExecutor(3, 10,
            5, TimeUnit.MINUTES, new LinkedBlockingDeque<>(),
            ThreadUtil.createThreadFactory("upload-video-executor"));

    private final VideoMapper videoMapper;

<<<<<<< HEAD

=======
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
    @Override
    public List<VideoSeriesVO> getUserSeries(Long userId) {
        return videoMapper.selectUserSeries(userId);
    }

    @Override
    public List<VideoSeriesVO> getSeriesVideos(Long seriesId) {
        return videoMapper.selectSeriesVideos(seriesId);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVideoSubtitle(String videoId, String subtitleUrl) {

        // 2. 更新字幕字段
        return this.lambdaUpdate()
                .eq(VideoEntity::getVideoId, videoId)
                .set(VideoEntity::getSubtitle, subtitleUrl)
                .update();
    }



    @Override
    public List<VideoStatsDTO> getUserVideoStatsLast7Days(Long userId) {
        return baseMapper.selectUserVideoStatsLast7Days(userId);
    }

    @Override
    public List<UserVideoStatsDto> selectUserVideoStatsGroupByDay(Long userId){
        return baseMapper.selectUserVideoStatsGroupByDay(userId);
    }


    @Override
    public List<ownVideoStatsDTO> getVideoStatsLast7Days(Long userId){
        return baseMapper.getVideoStatsLast7Days(userId);
    }

    @Override
    @Transactional(rollbackFor = SQLException.class)  // ✅ 正确写法
    public void changeVideo(ChangeVideoDTO videoDTO) {

        System.out.println("videoDTO:"+videoDTO);

        VideoEntity video = this.getById(videoDTO.getId());
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }


        if (!video.getUserId().equals(videoDTO.getUserId())) {
            throw new RuntimeException("无权修改此视频");
        }


        video.setTitle(videoDTO.getTitle());
        video.setDescription(videoDTO.getDescription());
        video.setArea(videoDTO.getArea());


        if (videoDTO.getCoverFile() != null && !videoDTO.getCoverFile().isEmpty()) {
            String coverUrl = ossService.uploadFile(videoDTO.getCoverFile());
            video.setCover(coverUrl);
        }


        System.out.println("video:"+video);

        this.updateById(video);
    }



    @Override
    public VideoDTO getVideoDetail(Long id) throws ClientException {
        VideoDTO videoDTO = baseMapper.getVideoDetail(id);
        Assert.notNull(videoDTO, "不存在的视频ID：{}", id);

        if (videoDTO.getStatus().equals(VideoStatusEnum.PUBLISHED)) {
            String videoPlayUrl = vodService.getVideoPlayUrl(videoDTO.getVideoId());
            videoDTO.setPlayUrl(videoPlayUrl);
        }

        long count = danmakuService.countByVideoId(id);
        videoDTO.setDanmakus(count);

        Long likeCount = videoLikeService.getLikeCount(id);
        videoDTO.setLike(likeCount);

        List<TagDTO> tags = videoTagService.listByVideoId(id);
        videoDTO.setTags(tags);
        return videoDTO;
    }

    @Override
    public IPage<VideoDTO> pageVideo(AdminVideoQueryParam param) {
        // 根据用户查询视频  根据视频描述信息模糊查询视频信息
        List<UserEntity> userEntityList = Collections.emptyList();
        if (StrUtil.isNotBlank(param.getUserName())) {
            userEntityList = userService.list(Wrappers.lambdaQuery(UserEntity.class).like(UserEntity::getNick, param.getUserName()));
            if (CollUtil.isEmpty(userEntityList)) {
                return new PageDTO<>(param.getCurrent(), param.getSize());
            }
        }
        Map<Long, String> userMap = userEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, UserEntity::getNick));
        Page<VideoEntity> page = page(new Page<>(param.getCurrent(), param.getSize()), Wrappers.lambdaQuery(VideoEntity.class)
                .in(CollUtil.isNotEmpty(userMap), VideoEntity::getUserId, new ArrayList<>(userMap.keySet()))
                .and(StrUtil.isNotBlank(param.getKeyword()), wrapper -> wrapper
                        .like(VideoEntity::getDescription, param.getKeyword())
                        .or()
                        .like(VideoEntity::getTitle, param.getKeyword())));


        if (Objects.isNull(page) || CollUtil.isEmpty(page.getRecords())) {
            return new PageDTO<>(param.getCurrent(), param.getSize());
        }
        IPage<VideoDTO> convert = PageUtils.convert(page, VideoDTO.class);

        List<VideoDTO> records = convert.getRecords();
        if (CollUtil.isNotEmpty(userMap)) {
            Map<Long, String> finalUserMap = userMap;
            records.forEach(x -> x.setNick(finalUserMap.get(x.getId())));
        } else {
            userEntityList = userService.list(Wrappers.lambdaQuery(UserEntity.class));
            userMap = userEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, UserEntity::getNick));
            Map<Long, String> finalUserMap1 = userMap;
            records.forEach(videoCommentEntity -> {
                videoCommentEntity.setNick(finalUserMap1.get(videoCommentEntity.getUserId()));
            });
        }

        List<Long> ids = records.stream().map(VideoDTO::getId).collect(Collectors.toList());
        List<VideoLikeEntity> likeList = Optional.ofNullable(videoLikeService.list(Wrappers.lambdaQuery(VideoLikeEntity.class)
                        .in(VideoLikeEntity::getVideoId, ids)))
                .orElseGet(ArrayList::new);
        Map<Long, Long> countMap = likeList.stream().collect(Collectors.groupingBy(VideoLikeEntity::getVideoId, Collectors.counting()));
        records.forEach(videoCommentEntity -> videoCommentEntity.setLike(countMap.get(videoCommentEntity.getUserId())));

        return convert;
    }

    @Override
    public IPage<VideoDTO> pageVideo(IPage<VideoDTO> page, VideoQueryParam param) {
        return baseMapper.pageVideo(page, param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadSeriesVideo(UploadSeriesVideoDTO videoDTO) throws IOException {
        // 检查上传限制
        Long userId = videoDTO.getUserId();
        String uploadKey = RedisKeys.VIDEO_UPLOAD + userId;
        String value = redisTemplate.opsForValue().get(uploadKey);
        if (value != null && Integer.parseInt(value) >= 10) {
            log.error("用户今日上传视频已经超过上限：{}", userId);
            throw new ApiException(ReturnCodeEnums.UPLOAD_VIDEO_LIMIT);
        }

        // 读取视频文件内容到字节数组
        byte[] videoBytes = videoDTO.getVideoFile().getBytes();

        // 保存基本信息
        VideoEntity newVideo = new VideoEntity();
        Long videoId = IdUtil.getSnowflakeNextId();

        // 设置视频基本信息
        newVideo.setId(videoId);
        newVideo.setDuration(videoDTO.getDuration());
        newVideo.setTitle(videoDTO.getTitle());
        newVideo.setDescription(videoDTO.getDescription());
        newVideo.setArea(videoDTO.getArea());
        newVideo.setUserId(userId);
        newVideo.setMd5(videoDTO.getMd5());
        newVideo.setStatus(VideoStatusEnum.UPLOADING);

        // 上传封面
        MultipartFile coverFile = videoDTO.getCoverFile();
        String coverUrl = ossService.uploadFile(coverFile);
        newVideo.setCover(coverUrl);

        // 保存视频基础信息到数据库
        this.save(newVideo);

        // 保存标签
        if (CollUtil.isNotEmpty(videoDTO.getTags())) {
            videoTagService.saveTags(newVideo.getId(), videoDTO.getTags());
        }

        // 处理分P视频信息
        VideoSeriesEntity seriesEntity = new VideoSeriesEntity();
        Long seriesId = videoDTO.getSeriesId() != null ? videoDTO.getSeriesId() : IdUtil.getSnowflakeNextId();

        seriesEntity.setId(IdUtil.getSnowflakeNextId());
        seriesEntity.setSeriesId(seriesId);
        seriesEntity.setVideoId(videoId);
        seriesEntity.setTitle(videoDTO.getPartTitle());
        seriesEntity.setDescription(videoDTO.getPartDescription());
        seriesEntity.setSortOrder(videoDTO.getSortOrder());

        videoSeriesService.save(seriesEntity);

        // 更新上传次数
        redisTemplate.opsForValue().increment(uploadKey);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ZonedDateTime zonedDateTime = tomorrow.atStartOfDay(ZoneId.systemDefault());
        Date expired = Date.from(zonedDateTime.toInstant());
        redisTemplate.expireAt(uploadKey, expired);

        // 在事务提交后执行异步操作（传递字节数组）
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                videoUploadExecutor.execute(() ->
                        processVideoUpload(videoId, videoBytes, coverUrl, videoDTO.getVideoFile().getOriginalFilename())
                );
            }
        });

        return videoId;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadVideo(UploadVideoDTO videoDTO) throws IOException {
        // 检查上传限制
        Long userId = videoDTO.getUserId();
        String uploadKey = RedisKeys.VIDEO_UPLOAD + userId;
        String value = redisTemplate.opsForValue().get(uploadKey);
        if (value != null && Integer.parseInt(value) >= 10) {
            log.error("用户今日上传视频已经超过上限：{}", userId);
            throw new ApiException(ReturnCodeEnums.UPLOAD_VIDEO_LIMIT);
        }

        // 读取视频文件内容到字节数组（关键修改点）
        byte[] videoBytes = videoDTO.getVideoFile().getBytes();

        // 保存基本信息
        VideoEntity newVideo = new VideoEntity();
        Long id = IdUtil.getSnowflakeNextId();

        // 设置视频基本信息
        newVideo.setId(id);
        newVideo.setDuration(videoDTO.getDuration());
        newVideo.setTitle(videoDTO.getTitle());
        newVideo.setDescription(videoDTO.getDescription());
        newVideo.setArea(videoDTO.getArea());
        newVideo.setUserId(userId);
        newVideo.setMd5(videoDTO.getMd5());
        newVideo.setStatus(VideoStatusEnum.UPLOADING);

        // 上传封面
        MultipartFile coverFile = videoDTO.getCoverFile();
        String coverUrl = ossService.uploadFile(coverFile);
        newVideo.setCover(coverUrl);

        // 保存视频基础信息到数据库
        this.save(newVideo);

        // 保存标签
        if (CollUtil.isNotEmpty(videoDTO.getTags())) {
            videoTagService.saveTags(newVideo.getId(), videoDTO.getTags());
        }

        // 更新上传次数
        redisTemplate.opsForValue().increment(uploadKey);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ZonedDateTime zonedDateTime = tomorrow.atStartOfDay(ZoneId.systemDefault());
        Date expired = Date.from(zonedDateTime.toInstant());
        redisTemplate.expireAt(uploadKey, expired);


        // 在事务提交后执行异步操作（传递字节数组）
        final Long videoId = id;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                videoUploadExecutor.execute(() ->
                        processVideoUpload(videoId, videoBytes, coverUrl, videoDTO.getVideoFile().getOriginalFilename())
                );
            }
        });

        return id;
    }


    @Override
    public List<VideoSeriesVO> getVideoSeries(Long Id) {
        // 1. 先通过 videoId 查询对应的 seriesId
        VideoSeriesEntity videoSeries = videoSeriesService.lambdaQuery()
                .eq(VideoSeriesEntity::getVideoId, Id)
                .one();

        if (videoSeries == null) {
            log.info("No series found for videoId: {}", Id);
            return Collections.emptyList();
        }

        Long seriesId = videoSeries.getSeriesId();
        List<VideoSeriesEntity> seriesList = videoSeriesService.lambdaQuery()
                .eq(VideoSeriesEntity::getSeriesId, seriesId)
                .orderByAsc(VideoSeriesEntity::getSortOrder)
                .list();

        if (CollUtil.isEmpty(seriesList)) {
            return Collections.emptyList();
        }

        List<Long> videoIds = seriesList.stream()
                .map(VideoSeriesEntity::getVideoId)
                .collect(Collectors.toList());

        Map<Long, VideoEntity> videoMap = this.listByIds(videoIds).stream()
                .collect(Collectors.toMap(VideoEntity::getId, Function.identity()));

        return seriesList.stream().map(series -> {
            VideoSeriesVO vo = new VideoSeriesVO();
            VideoEntity video = videoMap.get(series.getVideoId());

            vo.setSeriesId(series.getSeriesId());
            vo.setVideoId(series.getVideoId());
            vo.setPartTitle(series.getTitle());
            vo.setPartDescription(series.getDescription());
            vo.setSortOrder(series.getSortOrder());

            if (video != null) {
                vo.setTitle(video.getTitle());
                vo.setCover(video.getCover());
                vo.setDuration(video.getDuration());
                vo.setViewCount(video.getCount());
                vo.setStatus(video.getStatus());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    private void processVideoUpload(Long videoId, byte[] videoBytes, String coverUrl, String originalFilename) {
        try (InputStream inputStream = new ByteArrayInputStream(videoBytes)) {
            log.info("开始上传视频到云存储，videoId：{}", videoId);

            // 构造文件名（处理空文件名情况）
            String fileName = StringUtils.hasText(originalFilename) ?
                    originalFilename : "video_" + videoId;

            // 上传到云存储
            String cloudVideoId = vodService.upload(
                    fileName,
                    fileName,
                    coverUrl,
                    inputStream
            );

            // 更新视频状态
            VideoEntity video = this.getById(videoId);
            video.setVideoId(cloudVideoId)
                    .setStatus(VideoStatusEnum.PUBLISHED);
            this.updateById(video);

            // 通知前端
            webSocketServer.sendVideoStatusUpdate(
                    video.getUserId(),
                    videoId,
                    VideoStatusEnum.PUBLISHED,
                    "视频上传成功"
            );

            // 通知订阅者（根据业务需求实现）
            notifySubscribers(video);
        } catch (Exception e) {
            log.error("视频上传失败，videoId：{}", videoId, e);
            handleUploadFailure(videoId, e);
        }
    }



    private void handleUploadFailure(Long videoId, Exception e) {
        VideoEntity video = this.getById(videoId);
        video.setStatus(VideoStatusEnum.UPLOAD_FAILED);
        this.updateById(video);
        webSocketServer.sendVideoStatusUpdate(
                video.getUserId(),
                videoId,
                VideoStatusEnum.UPLOAD_FAILED,
                "视频上传失败：" + e.getMessage()
        );
    }





    /**
     * 通知订阅者有新视频上传
     */
    private void notifySubscribers(VideoEntity video) {
        // 获取上传者的所有订阅者
        List<Long> subscribers = userSubscriptionService.getSubscriberIds(video.getUserId());
        if (CollUtil.isNotEmpty(subscribers)) {
            // 获取上传者信息
            String uploaderName = userService.getById(video.getUserId()).getNick();
            // 批量发送通知
            webSocketServer.sendNewVideoNotificationBatch(subscribers, uploaderName, video);
        }
    }

    @Override
    public void incrViewCounts(Long id) {
        String key = RedisKeys.VIEW_COUNTS + id;
        if (redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().increment(RedisKeys.VIEW_COUNTS + id);
        } else {
            Long count = this.lambdaQuery()
                    .select(VideoEntity::getCount)
                    .eq(VideoEntity::getId, id)
                    .one().getCount();
            redisTemplate.opsForValue().set(key, (count + 1) + "");
        }


        lambdaUpdate()
                .setSql("count = count + 1")  // 直接写 SQL 表达式
                .eq(VideoEntity::getId, id)   // 这里可以用方法引用
                .update();
    }

    @Override
    public VideoLikeInfoDTO getVideoLikeInfo(Long videoId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        boolean isLike = videoLikeService.isLikeVideo(userId, videoId);
        boolean isCollect = videoCollectionService.isCollect(userId, videoId);
        VideoLikeInfoDTO videoLikeInfoDTO = new VideoLikeInfoDTO();
        videoLikeInfoDTO.setIsLike(isLike);
        videoLikeInfoDTO.setIsCollect(isCollect);
        return videoLikeInfoDTO;
    }

    @Override
    public List<VideoDTO> recommendVideosUserBased() throws TasteException {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        Long count = baseMapper.getPreferenceCountByUserId(userId);
        if (count < 5) {
            log.info("userId：{} 偏好视频数据太少", userId);
            return getRandomVideo();
        }
        DataModel dataModel = getDataModel();
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

        List<RecommendedItem> recommend = recommender.recommend(userId, 50);
        if (recommend.isEmpty()) {
            log.info("userId：{} 可推荐视频数据太少", userId);
            return getRandomVideo();
        }
        log.info("向 userId：{} 推荐视频，执行 Mahout 推荐算法", userId);
        List<Long> videoIds = recommend.stream()
                .map(RecommendedItem::getItemID)
                .distinct()  // 确保推荐结果中没有重复视频
                .collect(Collectors.toList());

        // 计算还需要补充的视频数量
        int needVideoSize = 7 - videoIds.size();
        if (needVideoSize > 0) {
            // 获取补充的视频ID，并确保不重复
            List<Long> additionalVideoIds = getRandomNeedVideo(needVideoSize, userId)
                    .stream()
                    .map(VideoDTO::getId)
                    .filter(id -> !videoIds.contains(id))  // 确保补充的视频不在原有推荐列表中
                    .collect(Collectors.toList());

            videoIds.addAll(additionalVideoIds);
        }
        // 如果最终视频数量仍不足7个，继续补充直到满足要求
        while (videoIds.size() < 7) {
            List<Long> extraVideoIds = getRandomNeedVideo(1, userId)
                    .stream()
                    .map(VideoDTO::getId)
                    .filter(id -> !videoIds.contains(id))
                    .collect(Collectors.toList());

            if (!extraVideoIds.isEmpty()) {
                videoIds.add(extraVideoIds.get(0));
            } else {
                break;  // 如果无法获取更多视频，则退出循环
            }
        }
        return Optional.ofNullable(getRandomVideo(videoIds)).orElseGet(this::getRandomVideo);
    }

    private List<VideoDTO> getRandomVideo(List<Long> videoIds) {
        return baseMapper.getRandomVideo(videoIds);
    }

    private List<VideoDTO> getRandomVideo() {
        return baseMapper.getRandomVideo(Collections.emptyList());
    }
    private List<VideoDTO> getRandomNeedVideo(int needVideoSize,Long userId) {
        return baseMapper.getRandomNeedVideo(needVideoSize,userId);
    }

    @Override
    public List<VideoDTO> recommendVideoItemBased(HttpServletRequest request) throws TasteException {
<<<<<<< HEAD
        // 使用策略模式重构后的推荐逻辑
        // 构建推荐上下文
        com.hsx.manyue.modules.video.recommendation.RecommendationContext context = 
            com.hsx.manyue.modules.video.recommendation.RecommendationContext.builder()
                .userId(JwtUtil.LOGIN_USER_HANDLER.get())
                .request(request)
                .videoMapper(videoMapper)
                .recommendCount(7)
                .build();
        
        // 策略工厂会自动选择合适的策略（用户登录 -> UserBasedStrategy，游客 -> ItemBasedStrategy）
        return recommendationStrategyFactory.recommend(context);
=======

        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        if (userId != null) {
            return recommendVideosUserBased();
        }else{

            System.out.println("内容推荐");

            //生成clientId
            String agent = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(agent);
            String clientId = String.valueOf(userAgent.getId());
            String ip = IpUtil.getIP(request);

            //根据clientId和ip去t_video_history表查找最新的视频，返回videoId，如果没有，就返回所有用户三天内浏览记录最多的videoId，
            Long videoId = getRecommendationSeed(clientId, ip);

            try {
                Long count = baseMapper.getPreferenceCountByVideoId(videoId);
                if (count < 5) {
                    log.info("videoId：{} 偏好视频数据太少", videoId);
                    return getRandomVideo();
                }

                DataModel dataModel = getDataModel();
                ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
                GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, similarity);

                List<RecommendedItem> recommendedItems;

                log.info("向游客用户提供基于视频：{}的推荐", videoId);
                 recommendedItems = recommender.mostSimilarItems(videoId, 50);


                if (recommendedItems.isEmpty()) {
                    log.info("videoId：{} 可推荐视频数据太少，随机推荐视频", videoId);
                    return getRandomVideo();
                }
                log.info("推荐 videoId：{} 相关视频，执行 Mahout 推荐算法", videoId);
                List<Long> videoIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
                return getRandomVideo(videoIds);
            } catch (NoSuchItemException e) {
                log.info("视频：{}相关数据稀少，随机推荐视频", videoId);
                return getRandomVideo();
            }
        }


    }

    private Long getRecommendationSeed(String clientId, String ip) {

        List<Map<String, Object>> ipHotVideos = null;

        if (clientId != null) {
            ipHotVideos = baseMapper.selectHotVideosByClientId(clientId,1);

        } else if( ip != null) {
            // 次之获取同IP用户最近热门视频
            ipHotVideos = baseMapper.selectHotVideosByIp(ip, 1);
        }
        if (!ipHotVideos.isEmpty()) {
            return (Long) ipHotVideos.get(0).get("video_id");
        }
        // 最后返回全站热门视频
        return baseMapper.selectGlobalHotVideo();
>>>>>>> 01c11183b8bed47b9ab614855691e58ba43b30d3
    }


    @Override
    public VideoDTO getVideoNotPlayUrl(Long videoId) {
        VideoDTO videoDTO = baseMapper.getVideoDetail(videoId);
        List<TagDTO> tags = videoTagService.listByVideoId(videoId);
        videoDTO.setTags(tags);
        return videoDTO;
    }

    @Override
    public List<VideoDTO> userVideos(Long userId) {
        Long currentUserId = JwtUtil.LOGIN_USER_HANDLER.get();
        return this.lambdaQuery()
                .eq(VideoEntity::getUserId, userId)
                // 用户查询自己的视频列表，查询所有视频状态，否则，只查询已发布视频
                .eq(currentUserId != null && !NumberUtil.equals(currentUserId, userId), VideoEntity::getStatus, VideoStatusEnum.PUBLISHED)
                .orderByDesc(VideoEntity::getCreateTime)
                .list()
                .stream()
                .map(i -> BeanUtil.toBean(i, VideoDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<VideoDTO> queryVideosByEs(VideoQueryParam param) {
        // 创建分页对象
        Page<VideoEntity> page = new Page<>(param.getCurrent(), param.getSize());
        String keyword = param.getKeyword();

        // 根据type区分搜索类型
        if ("user".equals(param.getType())) {
            // 用户搜索：在用户昵称中查找
            if (StrUtil.isNotBlank(keyword)) {
                List<UserEntity> userList = userService.lambdaQuery()
                        .like(UserEntity::getNick, "%" + keyword + "%")
                        .list();

                if (CollUtil.isEmpty(userList)) {
                    return new Page<>(param.getCurrent(), param.getSize());
                }

                // 获取匹配用户的ID
                List<Long> userIds = userList.stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toList());

                // 查询这些用户的视频
                page = this.lambdaQuery()
                        .in(VideoEntity::getUserId, userIds)
                        .eq(VideoEntity::getStatus, VideoStatusEnum.PUBLISHED)
                        .orderByDesc(VideoEntity::getCreateTime)
                        .page(page);
            } else {
                return new Page<>(param.getCurrent(), param.getSize());
            }
        } else {
            // 视频搜索：在标题、描述中查找
            LambdaQueryWrapper<VideoEntity> wrapper = Wrappers.lambdaQuery(VideoEntity.class)
                    .eq(VideoEntity::getStatus, VideoStatusEnum.PUBLISHED);

            if (StrUtil.isNotBlank(keyword)) {
                // 拆分关键词进行更精确的搜索
                String[] keywords = keyword.split(" ");
                wrapper.and(w -> {
                    for (String k : keywords) {
                        w.or().like(VideoEntity::getTitle, "%" + k + "%")
                                .or().like(VideoEntity::getDescription, "%" + k + "%");
                    }
                });
            }

            // 按区域筛选
            VideoAreaEnum area = param.getArea();
            if (area != null) {
                wrapper.eq(VideoEntity::getArea, area);
            }

            // 添加随机排序（模拟ES的随机排序功能）
            // 使用RAND()函数实现随机排序，或者使用固定种子提供一致性
            String randomOrderSql = param.getSeed() == 0
                    ? "RAND()"
                    : "RAND(" + param.getSeed() + ")";
            wrapper.last("ORDER BY " + randomOrderSql);

            page = this.page(page, wrapper);
        }

        // 如果没有结果，返回空页
        if (page.getRecords().isEmpty()) {
            return new Page<>(param.getCurrent(), param.getSize());
        }

        // 转换为DTO结果
        Page<VideoDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<VideoDTO> records = page.getRecords().stream().map(entity -> {
            VideoDTO dto = BeanUtil.toBean(entity, VideoDTO.class);

            // 填充用户信息
            UserEntity user = userService.getById(entity.getUserId());
            if (user != null) {
                dto.setNick(user.getNick());
                dto.setAvatar(user.getAvatar());
            }

            // 获取视频标签
            List<TagDTO> tags = videoTagService.listByVideoId(entity.getId());
            dto.setTags(tags);

            // 获取弹幕数量
            long danmakuCount = danmakuService.countByVideoId(entity.getId());
            dto.setDanmakus(danmakuCount);

            // 获取点赞数量
            Long likeCount = videoLikeService.getLikeCount(entity.getId());
            dto.setLike(likeCount);

            // 手动添加高亮（如果需要）
            if (StrUtil.isNotBlank(keyword) && StrUtil.isNotBlank(dto.getTitle())) {
                String title = dto.getTitle();
                int index = title.toLowerCase().indexOf(keyword.toLowerCase());
                if (index >= 0) {
                    String matched = title.substring(index, index + keyword.length());
                    dto.setTitle(title.replace(matched, "<strong class=\"keyword\">" + matched + "</strong>"));
                }
            }

            return dto;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public List<VideoDTO> areaVideo(Long area) {
        return baseMapper.getRandomVideoByArea(Objects.isNull(area) ? null : String.valueOf(area));
    }

    @Override
    public IPage<VideoDTO> getUserVideosPage(Long userId, Integer current, Integer size) {
        // 获取当前登录用户ID，用于判断是否查看自己的视频
        Long currentUserId = JwtUtil.LOGIN_USER_HANDLER.get();

        // 创建分页对象
        Page<VideoEntity> page = new Page<>(current, size);

        // 查询条件
        boolean isSelf = currentUserId != null && NumberUtil.equals(currentUserId, userId);

        // 查询视频列表
        page = this.lambdaQuery()
                .eq(VideoEntity::getUserId, userId)
                // 如果不是查询自己的视频，只显示已发布的视频
                .eq(!isSelf, VideoEntity::getStatus, VideoStatusEnum.PUBLISHED)
                .orderByDesc(VideoEntity::getCreateTime)
                .page(page);

        // 转换为DTO
        Page<VideoDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        resultPage.setRecords(page.getRecords().stream().map(entity -> {
            VideoDTO dto = BeanUtil.toBean(entity, VideoDTO.class);
            // 获取视频标签
            List<TagDTO> tags = videoTagService.listByVideoId(entity.getId());
            dto.setTags(tags);

            // 获取弹幕数量
            long danmakuCount = danmakuService.countByVideoId(entity.getId());
            dto.setDanmakus(danmakuCount);

            // 获取点赞数量
            Long likeCount = videoLikeService.getLikeCount(entity.getId());
            dto.setLike(likeCount);

            return dto;
        }).collect(Collectors.toList()));

        return resultPage;
    }

    private DataModel getDataModel() {
        List<UserPreference> userPreferences = baseMapper.getPreferenceData();

        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();

        List<List<UserPreference>> userPreferencesGroupByUserId = CollUtil.groupByField(userPreferences, "userId");
        for (List<UserPreference> preferences : userPreferencesGroupByUserId) {
            PreferenceArray preferenceArray = new GenericUserPreferenceArray(preferences);
            fastByIdMap.put(preferences.get(0).getUserID(), preferenceArray);
        }

        return new GenericDataModel(fastByIdMap);
    }

    @Override
    public List<String> getSearchSuggestions(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }

        // 使用MySQL的模糊查询来替代ES查询
        List<VideoEntity> videos = this.lambdaQuery()
                .select(VideoEntity::getTitle)
                .like(VideoEntity::getTitle, "%" + keyword + "%")
                .eq(VideoEntity::getStatus, VideoStatusEnum.PUBLISHED)
                .last("LIMIT 10") // 限制返回结果数量
                .list();

        // 如果没有匹配结果，返回空列表
        if (CollUtil.isEmpty(videos)) {
            return Collections.emptyList();
        }

        // 获取结果并手动添加高亮
        return videos.stream()
                .map(VideoEntity::getTitle)
                .map(title -> {
                    // 不管是中文还是英文，都使用简单的字符串替换方法来高亮
                    // 对大小写不敏感
                    int index = title.toLowerCase().indexOf(keyword.toLowerCase());
                    if (index >= 0) {
                        // 获取实际匹配的文本（保留原始大小写）
                        String matched = title.substring(index, index + keyword.length());
                        // 替换为高亮版本
                        return title.replace(matched, "<strong class=\"keyword\">" + matched + "</strong>");
                    }

                    // 如果上面的方法没有找到精确匹配，尝试查找包含关系
                    for (String word : title.split("\\s+")) {
                        if (word.toLowerCase().contains(keyword.toLowerCase())) {
                            int wordIndex = word.toLowerCase().indexOf(keyword.toLowerCase());
                            String matchedPart = word.substring(wordIndex, wordIndex + keyword.length());
                            String highlightedWord = word.replace(matchedPart,
                                    "<strong class=\"keyword\">" + matchedPart + "</strong>");
                            return title.replace(word, highlightedWord);
                        }
                    }

                    // 最后兜底，如果没有找到匹配，返回原始标题
                    return title;
                })
                .distinct() // 去重
                .collect(Collectors.toList());
    }

    @Override
    public List<VideoDTO> getNewVideoNotifications() {
        Long currentUserId = JwtUtil.LOGIN_USER_HANDLER.get();
        String key = RedisKeys.VIDEO_NOTIFICATIONS + currentUserId;
        List<Object> notifications = redisUtil.lGet(key, 0, -1);
        return notifications.stream()
                .map(Object::toString)
                .map(str -> JSONUtil.toBean(str, VideoDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean deleteVideo(String id) {
        VideoEntity byId = getById(id);
        if (byId == null) {
            throw new ApiException(ReturnCodeEnums.FAIL);
        }

        // 删除视频数据
        return removeById(id);
    }

    @Override
    public void insertDanMaKu(DanmakuEntity danmakuEntity) {
        baseMapper.insertDanMaKu(danmakuEntity);
    }

}
