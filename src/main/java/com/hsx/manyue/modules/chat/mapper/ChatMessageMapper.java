package com.hsx.manyue.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import org.apache.ibatis.annotations.Mapper;


public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    // 自定义方法示例
    int updateMessagesStatus(Long fromUserId, Long toUserId, Integer status);
    
    // 获取未读消息数量
    Long countUnreadMessages(Long userId);
}