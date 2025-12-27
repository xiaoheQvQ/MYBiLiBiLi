package com.hsx.manyue.modules.oss.service;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.hsx.manyue.common.constant.AliyunAiJobStatusConstant;

import java.io.InputStream;

public interface IVodService {

    /**
     * 上传视频流到阿里云VOD
     *
     * @param title       视频标题
     * @param fileName    带后缀的文件名
     * @param coverUrl    封面URL (可选)
     * @param inputStream 文件输入流
     * @return 阿里云VOD返回的 VideoId
     */
    String upload(String title, String fileName, String coverUrl, InputStream inputStream);


    /**
     * 获取视频信息
     *
     * @param videoId 阿里云视频ID
     * @return 阿里云视频信息
     * @throws ClientException 获取视频信息失败异常
     */
    GetVideoInfoResponse.Video getVideoInfo(String videoId) throws ClientException;

    /**
     * 获取视频播放地址
     *
     * @param videoId 阿里云视频ID
     * @return 阿里云视频播放地址
     * @throws ClientException 获取视频播放地址异常
     */
    String getVideoPlayUrl(String videoId) throws ClientException;


    String putAiReview(String videoId) throws ClientException;


    String getReviewResult(String jobId) throws ClientException;
}
