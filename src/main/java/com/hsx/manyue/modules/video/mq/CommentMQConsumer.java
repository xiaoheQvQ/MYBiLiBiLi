package com.hsx.manyue.modules.video.mq;

import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.config.MQConfig;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.video.mapper.VideoCommentMapper;
import com.hsx.manyue.modules.video.model.dto.CommentPublishDto;
import com.hsx.manyue.modules.video.model.dto.CommentReplyDto;
import com.hsx.manyue.modules.video.model.entity.CommentEntity;
import com.hsx.manyue.modules.video.service.IVideoCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论 MQ 消费者
 * 批量处理评论消息，提升数据库写入效率
 * 
 * 优化点：
 * 1. 支持批量消息接收（每批最多50条）
 * 2. 使用 MyBatis saveBatch 批量插入
 * 3. 异步处理，不阻塞用户请求
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommentMQConsumer {

    private final IVideoCommentService videoCommentService;
    private final VideoCommentMapper videoCommentMapper;
    private final WebSocketServer webSocketServer;

    /**
     * 消费评论消息（支持批量）
     * 
     * @param messages 消息列表
     */
    @RabbitListener(queues = MQConfig.COMMENT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveCommentMessages(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        log.info("开始批量处理评论消息，共 {} 条", messages.size());
        
        List<CommentEntity> commentEntities = new ArrayList<>();
        
        for (String message : messages) {
            try {
                // 尝试解析为评论发布消息
                if (message.contains("\"parentId\":0")) {
                    CommentPublishDto dto = JSONUtil.toBean(message, CommentPublishDto.class);
                    CommentEntity comment = buildCommentFromPublishDto(dto);
                    commentEntities.add(comment);
                } else {
                    // 解析为评论回复消息
                    CommentReplyDto dto = JSONUtil.toBean(message, CommentReplyDto.class);
                    CommentEntity reply = buildCommentFromReplyDto(dto);
                    commentEntities.add(reply);
                }
            } catch (Exception e) {
                log.error("解析评论消息失败: {}", message, e);
            }
        }

        // 批量保存评论
        if (!commentEntities.isEmpty()) {
            try {
                videoCommentService.saveBatch(commentEntities);
                log.info("批量保存评论成功，共 {} 条", commentEntities.size());
                
                // 发送 WebSocket 通知
                for (CommentEntity comment : commentEntities) {
                    sendWebSocketNotification(comment);
                }
            } catch (Exception e) {
                log.error("批量保存评论失败", e);
            }
        }
    }

    /**
     * 从评论发布DTO构建评论实体
     */
    private CommentEntity buildCommentFromPublishDto(CommentPublishDto dto) {
        CommentEntity comment = new CommentEntity();
        comment.setUserId(dto.getUserId());
        comment.setVideoId(dto.getVideoId());
        comment.setContent(dto.getContent());
        comment.setParentId(0L); // 新评论的parent_id设为0
        return comment;
    }

    /**
     * 从评论回复DTO构建评论实体
     */
    private CommentEntity buildCommentFromReplyDto(CommentReplyDto dto) {
        CommentEntity reply = new CommentEntity();
        reply.setUserId(dto.getUserId());
        reply.setVideoId(dto.getVideoId());
        reply.setParentId(dto.getParentId());
        reply.setContent(dto.getContent());
        reply.setToCreateTime(dto.getToCreateTime());
        reply.setReplyCommentId(dto.getReplyCommentId());
        reply.setToUserId(dto.getReplyToUserId());
        return reply;
    }

    /**
     * 发送 WebSocket 通知
     */
    private void sendWebSocketNotification(CommentEntity comment) {
        try {
            // 查询用户昵称和视频标题
            String nick = videoCommentMapper.getUserNickByUserId(comment.getUserId());
            String videoTitle = videoCommentMapper.getVideoTitleByVideoId(String.valueOf(comment.getVideoId()));
            String videoPublishUserId = videoCommentMapper.getvideoPublishUserIdByVideoId(comment.getVideoId());

            comment.setNick(nick);
            comment.setVideoTitle(videoTitle);
            comment.setVideoPublishUserId(videoPublishUserId);

            if (comment.getParentId() == 0) {
                webSocketServer.sendCommentLevel(comment);
            } else {
                webSocketServer.sendComment(comment);
            }
        } catch (Exception e) {
            log.error("发送 WebSocket 通知失败", e);
        }
    }
}
