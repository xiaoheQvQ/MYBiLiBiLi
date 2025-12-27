package com.hsx.manyue.modules.video.controller;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hsx.manyue.common.annotation.aspect.ApiLimit;
import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.danmaku.service.IDanmakuService;
import com.hsx.manyue.modules.oss.service.impl.OssServiceImpl;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.service.IUserService;
import com.hsx.manyue.modules.video.model.dto.*;
import com.hsx.manyue.modules.video.model.entity.TagEntity;
import com.hsx.manyue.modules.video.model.entity.VideoCommentEntity;
import com.hsx.manyue.modules.video.model.param.VideoCommentParam;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;

import com.hsx.manyue.modules.video.model.vo.VideoSeriesVO;
import com.hsx.manyue.modules.video.service.ITagService;
import com.hsx.manyue.modules.video.service.IVideoCollectionService;
import com.hsx.manyue.modules.video.service.IVideoCommentService;
import com.hsx.manyue.modules.video.service.IVideoLikeService;
import com.hsx.manyue.modules.video.service.IVideoService;
import com.hsx.manyue.modules.video.service.impl.VideoCommentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 视频表 前端控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@ApiLimit
public class VideoRestController extends SuperController {

    private final IVideoService videoService;
    private final IDanmakuService danmakuService;
    private final IVideoLikeService videoLikeService;
    private final IVideoCollectionService videoCollectionService;
    private final IVideoCommentService videoCommentService;
    private final ITagService tagService;
    private final IUserService userService;

    private final OssServiceImpl ossService;




    @GetMapping("/query")
    public R searchVideo(VideoQueryParam param) {
        // 根据type区分查询来源
        if ("user".equals(param.getType())) {
            // 用户查询直接走
            IPage<UserDTO> userResult = userService.getPage(param);
            return success(userResult);
        } else {
            // 视频查询走
            IPage<VideoDTO> result = videoService.queryVideosByEs(param);
            return success(result);
        }
    }



    @GetMapping
    @ApiLimit(count = 10)
    public R videoList(VideoQueryParam param) {
        return success(videoService.queryVideosByEs(param));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Login
    public R uploadVideo(@ModelAttribute @Validated UploadVideoDTO videoDTO) throws ClientException, IOException {
        return success(videoService.uploadVideo(videoDTO).toString());
    }


    @PostMapping(value = "/upload/series", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Login
    public R uploadSeriesVideo(@ModelAttribute @Validated UploadSeriesVideoDTO videoDTO) throws ClientException, IOException {
        return success(videoService.uploadSeriesVideo(videoDTO).toString());
    }

    @GetMapping("/series/{seriesId}")
    public R getVideoSeries(@PathVariable String seriesId) {
        return success(videoService.getVideoSeries(Long.valueOf(seriesId)));
    }

    @GetMapping("/user-series/{userId}")
    public R getUserSeries(@PathVariable String userId) {
        return success(videoService.getUserSeries(Long.valueOf(userId)));
    }

    @PostMapping(value = "/change", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Login
    public R changeVideo(@RequestPart("id") String id,
                         @RequestPart("title") String title,
                         @RequestPart(value = "description", required = false) String description,
                         @RequestPart("area") String area,
                         @RequestPart("tags") String tagsJson,
                         @RequestPart(value = "coverFile", required = false) MultipartFile coverFile) {

        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        // 通过 detail 匹配枚举
        VideoAreaEnum areaEnum = Arrays.stream(VideoAreaEnum.values())
                .filter(e -> e.getDetail().equals(area.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的视频分区: " + area));



        ChangeVideoDTO changeVideoDTO = new ChangeVideoDTO();
        changeVideoDTO.setId(Long.parseLong(id));
        changeVideoDTO.setUserId(userId);
        changeVideoDTO.setTitle(title);
        changeVideoDTO.setDescription(description);
        changeVideoDTO.setArea(areaEnum);
        changeVideoDTO.setTags(JSON.parseArray(tagsJson, TagDTO.class));
        changeVideoDTO.setCoverFile(coverFile);

        videoService.changeVideo(changeVideoDTO);
        return R.success();
    }


    /**
     * 吞吐量 40/s
     * 引入缓存后： 130/s
     */
    @GetMapping("/{id}")
    @ApiLimit(count = 10)
    public R getVideoDetail(@PathVariable Long id) throws ClientException {
        return success(videoService.getVideoDetail(id));
    }

    @GetMapping("/getUserVideoStatsLast7Days")
    public R getUserVideoStatsLast7Days(){
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        System.out.println("七日内："+videoService.getUserVideoStatsLast7Days(userId));
        return success(videoService.getUserVideoStatsLast7Days(userId));
    }


    @GetMapping("/selectUserVideoStatsGroupByDay")
    public R selectUserVideoStatsGroupByDay(){
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        System.out.println("七日内："+videoService.selectUserVideoStatsGroupByDay(userId));
        return success(videoService.selectUserVideoStatsGroupByDay(userId));
    }

    @GetMapping("/getVideoStatsLast7Days")
    public R getVideoStatsLast7Days(){
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        System.out.println("七日内："+videoService.getVideoStatsLast7Days(userId));
        return success(videoService.getVideoStatsLast7Days(userId));
    }

    @GetMapping("/like-info/{id}")
    @Login
    public R getVideoLikeInfo(@PathVariable Long id) {
        return success(videoService.getVideoLikeInfo(id));
    }

    @GetMapping("/list/{userId}")
    public R userVideos(@PathVariable Long userId) {
        List<VideoDTO> list = videoService.userVideos(userId);
        System.out.println("查询用户上传的视频列表："+list.toString());
        return success(list);
    }



    @GetMapping("/danmakus/v2/")
    public R getDanmakus(Long id) {
        List<Object[]> list = danmakuService.getDanmakusAsArrayInfoByVideoId(id);
        return this.success(list);
    }


    @PutMapping("/incr-view-counts/{id}")
    public R incrViewCounts(@PathVariable("id") Long id) {
        videoService.incrViewCounts(id);
        return R.success();
    }

    @PutMapping("/like/{id}")
    @Login
    public R like(@PathVariable Long id) {
        videoLikeService.likeVideo(id);
        return R.success();
    }

    @PutMapping("/collect/{id}")
    @Login
    public R collect(@PathVariable Long id) {
        videoCollectionService.collect(id);

        return R.success();
    }

    @GetMapping("/collections")
    @Login
    public R getCollections() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<VideoDTO> list = videoCollectionService.getCollections(userId);
        System.out.println("用户的收藏列表："+list.toString());
        return success(list);
    }


    @GetMapping("/comment/getAllCommentByVideoId")
    public R getAllCommentByVideoId(@RequestParam("videoId") Long videoId){

        // 获取评论列表
        List<CommentDto> comments = videoCommentService.getAllCommentByVideoId(videoId);

        // 转换为JSON字符串
        String json = JSON.toJSONString(comments, true); // true参数表示格式化输出
        System.out.println("获取全部评论(JSON格式): " + json);
        return R.success(videoCommentService.getAllCommentByVideoId(videoId));
    }

    @Login
    @PostMapping("/comment/publish")
    public R publishComment(@RequestBody CommentPublishDto commentPublishDto) {
        System.out.println("发表评论");
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        commentPublishDto.setUserId(userId);
        return videoCommentService.publishComment(commentPublishDto);
    }

    @Login
    @PostMapping("/comment/reply")
    public R replyComment(@RequestBody CommentReplyDto commentReplyDto) {
        System.out.println("回复评论");
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        commentReplyDto.setUserId(userId);
        return videoCommentService.replyComment(commentReplyDto);
    }

    @Login
    @PostMapping("/comment/delete")
    public R deleteComment(@Param("commentId") Long commentId) {

        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();

        return videoCommentService.deleteComment(commentId,userId);
    }

    @PostMapping("/comment/img")
    public String CommentImg(MultipartFile file) {

        return ossService.uploadFile(file);
    }



    @GetMapping("/areas")
    public R areas() {
        return this.success(VideoAreaEnum.getCodeAndDetail());
    }

    @GetMapping("/areaVideo/{area}")
    public R areaVideo(@PathVariable String area) {
        Long code;
        if (area.equals("全部")) {
            code = null;
        } else {
            code = VideoAreaEnum.getCodeByArea(area);
            System.out.println(code);
        }

        List<VideoDTO> list = videoService.areaVideo(code);
        return this.success(list);
    }

    @GetMapping("/tags")
    public R tags() {
        List<TagDTO> tags = tagService.getTags();
        return this.success(tags);
    }


    @GetMapping("/recommend")
    @Login
    public R recommendVideosUserBased() throws TasteException {
        List<VideoDTO> list = videoService.recommendVideosUserBased();
        return this.success(list);
    }

    @GetMapping("/recommend/{videoId}")
    public R recommendVideoItemBased(@PathVariable Long videoId) throws TasteException {
        //List<VideoDTO> list = videoService.recommendVideoItemBased(videoId);
        return this.success();
    }

    @GetMapping("/recommendItem")
    public R recommendVideoItemBased(HttpServletRequest request) throws TasteException {

        List<VideoDTO> list = videoService.recommendVideoItemBased(request);
        return this.success(list);
    }



    @GetMapping("/user-videos")
    public R getUserVideos(@RequestParam Long userId,
                           @RequestParam(defaultValue = "1") Integer current,
                           @RequestParam(defaultValue = "12") Integer size) {
        // 获取分页视频列表
        IPage<VideoDTO> result = videoService.getUserVideosPage(userId, current, size);
        return success(result);
    }

    @GetMapping("/input/query")
    public R getSearchSuggestions(@RequestParam String keyword) {
        List<String> suggestions = videoService.getSearchSuggestions(keyword);
        return success(suggestions);
    }

    @GetMapping("/new-video-notifications")
    @Login
    public R getNewVideoNotifications() {
        List<VideoDTO> newVideoNotifications = videoService.getNewVideoNotifications();
        return success(newVideoNotifications);
    }

}
