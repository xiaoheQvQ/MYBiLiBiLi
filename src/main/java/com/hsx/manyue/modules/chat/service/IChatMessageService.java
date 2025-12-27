package com.hsx.manyue.modules.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;

import java.util.List;


public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 保存私聊消息
     */
    ChatMessage savePrivateMessage(Long fromUserId, Long toUserId, String content);

    /**
     * 获取聊天历史
     */
    List<ChatMessage> getChatHistory(Long fromUserId, Long toUserId, int limit);

    /**
     * 更新消息状态
     */
    boolean updateMessagesStatus(Long fromUserId, Long toUserId, Integer status);

    /**
     * 获取未读消息数量
     */
    Long countUnreadMessages(Long userId);
}