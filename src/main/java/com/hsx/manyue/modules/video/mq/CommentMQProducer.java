package com.hsx.manyue.modules.video.mq;

import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.config.MQConfig;
import com.hsx.manyue.common.mq.ReliableMessageSender;
import com.hsx.manyue.modules.video.model.dto.CommentPublishDto;
import com.hsx.manyue.modules.video.model.dto.CommentReplyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 评论 MQ 生产者
 * 将评论发布操作异步化，通过 MQ 进行流量削峰
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommentMQProducer {

    private final ReliableMessageSender reliableMessageSender;

    /**
     * 发送评论发布消息
     * 
     * @param commentPublishDto 评论发布DTO
     */
    public void sendCommentPublishMessage(CommentPublishDto commentPublishDto) {
        try {
            String message = JSONUtil.toJsonStr(commentPublishDto);
            reliableMessageSender.sendMessageReliably(
                    MQConfig.COMMENT_EXCHANGE,
                    MQConfig.COMMENT_ROUTING_KEY,
                    message
            );
            log.info("评论发布消息发送成功，userId: {}, videoId: {}", 
                    commentPublishDto.getUserId(), commentPublishDto.getVideoId());
        } catch (Exception e) {
            log.error("评论发布消息发送失败", e);
            throw new RuntimeException("评论发布失败，请稍后重试");
        }
    }

    /**
     * 发送评论回复消息
     * 
     * @param commentReplyDto 评论回复DTO
     */
    public void sendCommentReplyMessage(CommentReplyDto commentReplyDto) {
        try {
            String message = JSONUtil.toJsonStr(commentReplyDto);
            reliableMessageSender.sendMessageReliably(
                    MQConfig.COMMENT_EXCHANGE,
                    MQConfig.COMMENT_ROUTING_KEY,
                    message
            );
            log.info("评论回复消息发送成功，userId: {}, parentId: {}", 
                    commentReplyDto.getUserId(), commentReplyDto.getParentId());
        } catch (Exception e) {
            log.error("评论回复消息发送失败", e);
            throw new RuntimeException("评论回复失败，请稍后重试");
        }
    }
}
