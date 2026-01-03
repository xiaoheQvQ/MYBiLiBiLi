package com.hsx.manyue.modules.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.im.model.entity.IMConversationEntity;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import com.hsx.manyue.modules.im.model.vo.IMConversationVO;

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
     * 更新或创建会话 (支持@)
     */
    void updateConversation(IMMessageEntity message, java.util.List<Long> atUserIds, Boolean atAll);

    /**
     * 更新或创建会话（指定内容）
     */
    void updateOrCreateConversation(Long userId, Integer conversationType, Long targetId, String lastMsgContent, Long lastMsgSeq);
    
    /**
     * 获取用户会话列表（包含目标用户/群组信息）
     */
    List<IMConversationVO> getConversationList(Long userId);
    
    /**
     * 清除未读数
     */
    void clearUnread(Long userId, Integer conversationType, Long targetId);
    
    /**
     * 删除会话
     */
    void deleteConversation(Long userId, Integer conversationType, Long targetId);

    /**
     * 置顶/取消置顶会话
     */
    void pinConversation(Long userId, Integer conversationType, Long targetId, Boolean isTop);

    /**
     * 免打扰/取消免打扰
     */
    void muteConversation(Long userId, Integer conversationType, Long targetId, Boolean isMute);
}
