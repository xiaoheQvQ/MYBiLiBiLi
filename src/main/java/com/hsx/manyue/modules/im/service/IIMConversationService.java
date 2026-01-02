package com.hsx.manyue.modules.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.im.model.entity.IMConversationEntity;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;

import java.util.List;

/**
 * IM会话服务接口
 */
public interface IIMConversationService extends IService<IMConversationEntity> {
    
    /**
     * 更新或创建会话
     */
    void updateConversation(IMMessageEntity message);
    
    /**
     * 获取用户会话列表
     */
    List<IMConversationEntity> getConversationList(Long userId);
    
    /**
     * 清除未读数
     */
    void clearUnread(Long userId, Integer conversationType, Long targetId);
    
    /**
     * 删除会话
     */
    void deleteConversation(Long userId, Integer conversationType, Long targetId);
}
