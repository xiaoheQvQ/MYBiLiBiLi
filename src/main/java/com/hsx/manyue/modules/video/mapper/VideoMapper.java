package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;
import com.hsx.manyue.modules.video.model.dto.*;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import com.hsx.manyue.modules.video.model.vo.VideoSeriesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 视频表 Mapper 接口
 */

public interface VideoMapper extends BaseMapper<VideoEntity> {

    /**
     * 通过md5获取video
     *
     * @param md5
     * @return
     */
    @Select("select * from t_video where md5 = #{md5} and is_delete = 0 limit 1")
    VideoEntity getByMd5(@Param("md5") String md5);

    VideoDTO getVideoDetail(Long id);

    IPage<VideoDTO> pageVideo(@Param("page") IPage<VideoDTO> page, @Param("param") VideoQueryParam param);

    /**
     * 查询用户偏好数据
     *
     * @return
     */
    List<UserPreference> getPreferenceData();

    Long getPreferenceCountByUserId(@Param("userId") Long userId);

    List<VideoDTO> getRandomVideo(@Param("videoIds") List<Long> videoIds);

    List<VideoDTO> getRandomNeedVideo(
            @Param("needVideoSize") int needVideoSize,
            @Param("userId") Long userId
    );

    List<VideoDTO> getRandomVideoByArea(@Param("area") String area);

    Long getPreferenceCountByVideoId(Long videoId);


    @Select("SELECT video_id, COUNT(1) as view_count " +
            "FROM t_video_history " +
            "WHERE ip = #{ip} AND create_time > DATE_SUB(NOW(), INTERVAL 3 DAY) " +
            "GROUP BY video_id " +
            "ORDER BY view_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHotVideosByIp(String ip, int limit);


    @Select("SELECT video_id FROM t_video_history " +
            "WHERE create_time > DATE_SUB(NOW(), INTERVAL 3 DAY) " +
            "GROUP BY video_id " +
            "ORDER BY COUNT(1) DESC " +
            "LIMIT 1")
    Long selectGlobalHotVideo();

    @Select("SELECT video_id, COUNT(1) as view_count " +
            "FROM t_video_history " +
            "WHERE client_id = #{clientId} AND create_time > DATE_SUB(NOW(), INTERVAL 3 DAY) " +
            "GROUP BY video_id " +
            "ORDER BY view_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHotVideosByClientId(String clientId, int limit);

    void insertDanMaKu(DanmakuEntity danmakuEntity);

    List<VideoStatsDTO> selectUserVideoStatsLast7Days(Long userId);

    /**
     * 按天分组查询用户近7天视频数据统计（返回 DTO 列表）
     */
    List<UserVideoStatsDto> selectUserVideoStatsGroupByDay(@Param("userId") Long userId);

    List<ownVideoStatsDTO>  getVideoStatsLast7Days(@Param("userId") Long userId);



    // 获取用户的分P视频系列
    @Select("SELECT vs.series_id as seriesId, v.title, COUNT(vs.video_id) as videoCount " +
            "FROM t_video_series vs " +
            "JOIN t_video v ON vs.video_id = v.id " +
            "WHERE v.user_id = #{userId} " +
            "GROUP BY vs.series_id, v.title")
    List<VideoSeriesVO> selectUserSeries(@Param("userId") Long userId);

    // 获取系列视频详情
    @Select("SELECT vs.*, v.title, v.cover, v.duration, v.count as viewCount, v.status " +
            "FROM t_video_series vs " +
            "JOIN t_video v ON vs.video_id = v.id " +
            "WHERE vs.series_id = #{seriesId} " +
            "ORDER BY vs.sort_order")
    List<VideoSeriesVO> selectSeriesVideos(@Param("seriesId") Long seriesId);

}
