package com.hsx.manyue.modules.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.video.mapper.VideoCommentMapper;
import com.hsx.manyue.modules.video.model.dto.CommentDto;
import com.hsx.manyue.modules.video.model.dto.CommentPublishDto;
import com.hsx.manyue.modules.video.model.dto.CommentReplyDto;
import com.hsx.manyue.modules.video.model.entity.CommentEntity;
import com.hsx.manyue.modules.video.model.entity.VideoCommentEntity;
import com.hsx.manyue.modules.video.model.param.VideoCommentParam;
import com.hsx.manyue.modules.video.service.IVideoCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 视频评论表 服务实现类
 */
@RequiredArgsConstructor
@Service
public class VideoCommentServiceImpl extends ServiceImpl<VideoCommentMapper, CommentEntity> implements IVideoCommentService {


    private final WebSocketServer webSocketServer;

    @Override
    public R publishComment(CommentPublishDto commentPublishDto) {

        CommentEntity comment = new CommentEntity();
        comment.setUserId(commentPublishDto.getUserId());
        comment.setVideoId(commentPublishDto.getVideoId());
        comment.setContent(commentPublishDto.getContent());
        comment.setParentId(0L); // 新评论的parent_id设为0



        boolean success = save(comment);
        if (success) {
            String nick = baseMapper.getUserNickByUserId(commentPublishDto.getUserId());
            String videoTitle = baseMapper.getVideoTitleByVideoId(String.valueOf(commentPublishDto.getVideoId()));
            String videoPublishUserId = baseMapper.getvideoPublishUserIdByVideoId(commentPublishDto.getVideoId());

            comment.setNick(nick);
            comment.setVideoTitle(videoTitle);
            comment.setVideoPublishUserId(videoPublishUserId);

            webSocketServer.sendCommentLevel(comment);
            return R.success("评论发布成功");
        }
        return R.failure("评论发布失败");
    }

    @Override
    public R replyComment(CommentReplyDto commentReplyDto) {
        System.out.println("回复内容："+commentReplyDto);


        CommentEntity reply = new CommentEntity();
        reply.setUserId(commentReplyDto.getUserId());
        reply.setVideoId(commentReplyDto.getVideoId());
        reply.setParentId(commentReplyDto.getParentId());
        reply.setContent(commentReplyDto.getContent());

        //非数据库字段
        reply.setToCreateTime(commentReplyDto.getToCreateTime());
        reply.setReplyCommentId(commentReplyDto.getReplyCommentId());

        boolean success = save(reply);
        if (success) {
           // Long toUserId =  baseMapper.getUserIdByParentId(commentReplyDto.getReplyCommentId(),reply.getToCreateTime());
           // reply.setToUserId(toUserId);
            String nick = baseMapper.getUserNickByUserId(commentReplyDto.getReplyToUserId());
            String videoPublishUserId = baseMapper.getvideoPublishUserIdByVideoId(commentReplyDto.getVideoId());
            String videoTitle = baseMapper.getVideoTitleByVideoId(videoPublishUserId);
            reply.setNick(nick);
            reply.setVideoPublishUserId(videoPublishUserId);
            reply.setVideoTitle(videoTitle);
            reply.setToUserId(commentReplyDto.getReplyToUserId());
            System.out.println("测试11111111111111111111111111111111");
            webSocketServer.sendComment(reply);
            return R.success("回复成功");
        }
        return R.failure("回复失败");
    }

    @Override
    public R deleteComment(Long commentId, Long userId) {
        // 1. 验证评论是否存在
        CommentEntity comment = getById(commentId);
        if (comment == null || comment.getIsDelete() == 1) {
            return R.failure("评论不存在或已被删除");
        }

        // 2. 权限验证：只有评论作者或管理员可以删除
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isAdmin = "0".equals(baseMapper.queryAdmin(userId));

        if (!isCommentAuthor && !isAdmin) {
            return R.failure("无权删除该评论");
        }


        // 3. 递归删除评论及其所有子评论
        boolean success = recursiveDeleteComment(commentId);

        if (success) {
            return R.success("评论删除成功");
        }
        return R.failure("评论删除失败");
    }

    /**
     * 递归删除评论及其所有子评论
     */
    private boolean recursiveDeleteComment(Long commentId) {
        // 1. 查找所有子评论
        List<CommentEntity> children = lambdaQuery()
                .eq(CommentEntity::getParentId, commentId)
                .eq(CommentEntity::getIsDelete, 0)
                .list();

        // 2. 递归删除子评论
        for (CommentEntity child : children) {
            recursiveDeleteComment(child.getId());
        }

        // 3. 删除当前评论（逻辑删除）
        return lambdaUpdate()
                .eq(CommentEntity::getId, commentId)
                .set(CommentEntity::getIsDelete, 1)
                .set(CommentEntity::getUpdateTime, new Date())
                .update();
    }



    @Override
    public List<CommentDto> getAllCommentByVideoId(Long videoId) {
        // 查询一级评论（parent_id=0的评论）
        List<CommentDto> rootComments = baseMapper.getCommentByVideoId(videoId);

        // 递归获取所有子评论
        return buildCommentTree(rootComments);
    }

    private List<CommentDto> buildCommentTree(List<CommentDto> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        // 为每个评论获取子评论
        for (CommentDto comment : comments) {
            List<CommentDto> replies = baseMapper.getCommentByParentId(comment.getId());
            comment.setSons(buildCommentTree(replies));
        }

        return comments;
    }
}