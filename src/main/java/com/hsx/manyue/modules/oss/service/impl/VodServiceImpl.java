package com.hsx.manyue.modules.oss.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.*;
import com.hsx.manyue.common.config.AliyunProperty;
import com.hsx.manyue.common.constant.AliyunAiJobStatusConstant;
import com.hsx.manyue.modules.oss.service.IVodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Base64;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.hsx.manyue.common.enums.ReturnCodeEnums;
import com.hsx.manyue.common.exception.ApiException;


/**
 * 阿里云视频点播服务类实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VodServiceImpl implements IVodService {

    private final DefaultAcsClient acsClient;
    private final AliyunProperty aliyunProperty;

    private DefaultAcsClient initVodClient() {
        AliyunProperty.VodProperty vodProperty = aliyunProperty.getVodProperty();
        String regionId = vodProperty.getRegionId();
        DefaultProfile profile = DefaultProfile.getProfile(regionId, vodProperty.getAccessKeyId(), vodProperty.getAccessKeySecret());
        return new DefaultAcsClient(profile);
    }
    private CreateUploadVideoResponse createUploadVideo(DefaultAcsClient client, String title, String fileName, String coverUrl) throws ClientException {
        CreateUploadVideoRequest request = new CreateUploadVideoRequest();
        request.setTitle(title);
        request.setFileName(fileName);
        if (coverUrl != null && !coverUrl.isEmpty()) {
            request.setCoverURL(coverUrl);
        }
        return client.getAcsResponse(request);
    }
    private void uploadToOSS(CreateUploadVideoResponse uploadAddressResponse, InputStream inputStream) {
        String uploadAddressStr = new String(Base64.getDecoder().decode(uploadAddressResponse.getUploadAddress()));
        String uploadAuthStr = new String(Base64.getDecoder().decode(uploadAddressResponse.getUploadAuth()));

        JSONObject addressJson = JSONObject.parseObject(uploadAddressStr);
        String endpoint = addressJson.getString("Endpoint");
        String bucket = addressJson.getString("Bucket");
        String objectKey = addressJson.getString("FileName");

        JSONObject authJson = JSONObject.parseObject(uploadAuthStr);
        String stsAccessKeyId = authJson.getString("AccessKeyId");
        String stsAccessKeySecret = authJson.getString("AccessKeySecret");
        String securityToken = authJson.getString("SecurityToken");

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, stsAccessKeyId, stsAccessKeySecret, securityToken);
            ossClient.putObject(bucket, objectKey, inputStream);
            log.info("OSS upload completed successfully. Bucket: {}, ObjectKey: {}", bucket, objectKey);
        } catch (Exception e) {
            log.error("OSS upload failed. Error message: {}", e.getMessage(), e);
            throw new ApiException(ReturnCodeEnums.UPLOAD_VIDEO_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String upload(String title, String fileName, String coverUrl, InputStream inputStream) {
        DefaultAcsClient vodClient = initVodClient();
        try {
            log.info("Step 1: Getting VOD upload credential. Title: {}, FileName: {}", title, fileName);
            CreateUploadVideoResponse response = createUploadVideo(vodClient, title, fileName, coverUrl);
            String videoId = response.getVideoId();
            log.info("Step 1: Got VOD upload credential successfully. VideoId: {}", videoId);

            log.info("Step 2: Uploading file to OSS for VideoId: {}", videoId);
            try (InputStream stream = inputStream) {
                uploadToOSS(response, stream);
            }
            log.info("Step 2: File upload to OSS completed for VideoId: {}", videoId);
            return videoId;
        } catch (ClientException e) {
            log.error("Failed to get VOD upload credential. ErrorCode: {}, ErrorMessage: {}", e.getErrCode(), e.getErrMsg(), e);
            throw new ApiException(ReturnCodeEnums.UPLOAD_VIDEO_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred during video upload.", e);
            throw new ApiException(ReturnCodeEnums.UPLOAD_VIDEO_ERROR);
        }
    }



    @Override
    public GetVideoInfoResponse.Video getVideoInfo(String videoId) throws ClientException {
        GetVideoInfoRequest request = new GetVideoInfoRequest();
        request.setVideoId(videoId);
        GetVideoInfoResponse response = acsClient.getAcsResponse(request);
        Assert.notNull(response.getVideo(), "获取不到视频信息，videoId：{}", videoId);
        return response.getVideo();
    }


    @Override
    public String getVideoPlayUrl(String videoId) throws ClientException {
        GetPlayInfoRequest request = new GetPlayInfoRequest();
        request.setVideoId(videoId);
        GetPlayInfoResponse response = acsClient.getAcsResponse(request);
        return response.getPlayInfoList().get(0).getPlayURL();
    }

    @Override
    public String putAiReview(String videoId) throws ClientException {
        SubmitAIMediaAuditJobRequest request = new SubmitAIMediaAuditJobRequest();
        request.setMediaId(videoId);
        request.setTemplateId(aliyunProperty.getVodProperty().getAiReviewTemplateId());
        SubmitAIMediaAuditJobResponse response = acsClient.getAcsResponse(request);
        return response.getJobId();
    }

    @Override
    public String getReviewResult(String jobId) throws ClientException {
        GetAIMediaAuditJobRequest jobRequest = new GetAIMediaAuditJobRequest();
        jobRequest.setJobId(jobId);
        GetAIMediaAuditJobResponse jobResponse = acsClient.getAcsResponse(jobRequest);
        String jobStatus = jobResponse.getMediaAuditJob().getStatus();
        if (!StrUtil.equals(jobStatus, AliyunAiJobStatusConstant.JOB_SUCCESS)) {
            return jobStatus;
        }

        GetMediaAuditResultRequest request = new GetMediaAuditResultRequest();
        request.setMediaId(jobResponse.getMediaAuditJob().getMediaId());
        GetMediaAuditResultResponse response = acsClient.getAcsResponse(request);
        return response.getMediaAuditResult().getSuggestion();
    }
}
