package com.hsx.manyue.modules.im.service;

import com.hsx.manyue.modules.im.model.dto.IMMessageDTO;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;

import java.util.List;

/**
 * IM消息服务接口
 */
public interface IIMMessageService {
    
    /**
     * 发送单聊消息
     */
    IMMessageEntity sendSingleMessage(IMMessageDTO message);
    
    /**
     * 发送群聊消息
     */
    IMMessageEntity sendGroupMessage(IMMessageDTO message);
    
    /**
     * 确认消息送达
     */
    void ackMessage(Long msgSeq, Long userId);
    
    /**
     * 标记消息已读
     */
    void readMessage(Long msgSeq, Long userId);
    
    /**
     * 拉取历史消息
     */
    List<IMMessageEntity> pullHistory(Long userId, Long targetId, Integer sessionType, Long startSeq, Integer limit);
    
    /**
     * 获取未读消息数
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 同步消息
     */
    List<IMMessageEntity> syncMessages(Long userId, Long lastSeq);
    
    /**
     * 检查消息是否重复
     */
    boolean isDuplicate(String clientMsgId);
}
