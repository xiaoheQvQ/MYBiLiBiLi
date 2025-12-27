package com.hsx.manyue.modules.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.video.model.dto.CommentDto;
import com.hsx.manyue.modules.video.model.entity.CommentEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 视频评论表 Mapper 接口
 */
public interface VideoCommentMapper extends BaseMapper<CommentEntity> {


    List<CommentDto> getCommentByVideoId(@Param("videoId") Long videoId);
    List<CommentDto> getCommentByParentId(@Param("parentId") Long parentId);


    String getUserNickByUserId(Long replyToUserId);

    String getvideoPublishUserIdByVideoId(Long videoId);

    String getVideoTitleByVideoId(String videoPublishUserId);

    String queryAdmin(Long userId);
}