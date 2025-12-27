package com.hsx.manyue.modules.video.service;

import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;
import com.hsx.manyue.modules.video.model.dto.*;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.model.param.AdminVideoQueryParam;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import com.hsx.manyue.modules.video.model.vo.VideoSeriesVO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.IOException;
import java.util.List;

/**
 * 视频表 服务类
 */
public interface IVideoService extends IService<VideoEntity> {

    /**
     * 上传视频
     *
     * @param videoDTO 视频上传参数
     * @return 数据库视频ID
     */
    Long uploadVideo(UploadVideoDTO videoDTO) throws ClientException, IOException;

    VideoDTO getVideoDetail(Long id) throws ClientException;

    IPage<VideoDTO> pageVideo(AdminVideoQueryParam param);

    IPage<VideoDTO> pageVideo(IPage<VideoDTO> page, VideoQueryParam param);

    void incrViewCounts(Long id);


    VideoLikeInfoDTO getVideoLikeInfo(Long videoId);

    List<VideoDTO> recommendVideosUserBased() throws TasteException;

    List<VideoDTO> recommendVideoItemBased(HttpServletRequest request) throws TasteException;

    VideoDTO getVideoNotPlayUrl(Long videoId);

    List<VideoDTO> userVideos(Long userId);

    IPage<VideoDTO> queryVideosByEs(VideoQueryParam param);

    List<VideoDTO> areaVideo(Long area);

    /**
     * 根据用户ID分页查询视频列表
     *
     * @param userId  用户ID
     * @param current 当前页
     * @param size    每页大小
     * @return 分页结果
     */
    IPage<VideoDTO> getUserVideosPage(Long userId, Integer current, Integer size);

    List<String> getSearchSuggestions(String keyword);

    List<VideoDTO> getNewVideoNotifications();

    Boolean deleteVideo(String id);

    void insertDanMaKu(DanmakuEntity danmakuEntity);

    List<VideoStatsDTO> getUserVideoStatsLast7Days(Long userId);

    List<UserVideoStatsDto> selectUserVideoStatsGroupByDay(Long userId);

    List<ownVideoStatsDTO>  getVideoStatsLast7Days(@Param("userId") Long userId);

    void changeVideo(ChangeVideoDTO videoDTO);

    boolean updateVideoSubtitle(String videoId, String ccc);


    Long uploadSeriesVideo(UploadSeriesVideoDTO videoDTO) throws IOException;

    List<VideoSeriesVO> getVideoSeries(Long seriesId);

    List<VideoSeriesVO> getUserSeries(Long userId);

    List<VideoSeriesVO> getSeriesVideos(Long seriesId);

}
