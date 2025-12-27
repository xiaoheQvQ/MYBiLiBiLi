package com.hsx.manyue.modules.video.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.modules.video.model.dto.CommentDto;
import com.hsx.manyue.modules.video.model.dto.CommentPublishDto;
import com.hsx.manyue.modules.video.model.dto.CommentReplyDto;
import com.hsx.manyue.modules.video.model.entity.CommentEntity;
import com.hsx.manyue.modules.video.model.entity.VideoCommentEntity;
import com.hsx.manyue.modules.video.model.param.VideoCommentParam;

import java.util.List;

/**
 * 视频评论表 服务类
 */
public interface IVideoCommentService extends IService<CommentEntity> {


    R publishComment(CommentPublishDto commentPublishDto);

    R replyComment(CommentReplyDto commentReplyDto);

    R deleteComment(Long commentId, Long userId);

    List<CommentDto> getAllCommentByVideoId(Long videoId);
}