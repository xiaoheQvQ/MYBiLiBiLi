package com.hsx.manyue.modules.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.im.mapper.IMConversationMapper;
import com.hsx.manyue.modules.im.model.entity.IMConversationEntity;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import com.hsx.manyue.modules.im.service.IIMConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * IM会话服务实现
 */
@Slf4j
@Service
public class IMConversationServiceImpl extends ServiceImpl<IMConversationMapper, IMConversationEntity> 
        implements IIMConversationService {

    @Override
    @Transactional
    public void updateConversation(IMMessageEntity message) {
        // 更新发送者的会话 (只有单聊需要, 群聊不需要, 因为发送者已经在会话列表中了, 且发完后未读数也是0)
        // 实际上发送者和接收者都需要更新会话记录
        
        // 1. 更新接收者的会话
        updateOrCreateConversation(message.getToUserId(), message.getSessionType(), 
                message.getSessionType() == 1 ? message.getFromUserId() : message.getToGroupId(), 
                message, true);
        
        // 2. 更新发送者的会话
        updateOrCreateConversation(message.getFromUserId(), message.getSessionType(),
                message.getSessionType() == 1 ? message.getToUserId() : message.getToGroupId(), 
                message, false);
    }

    private void updateOrCreateConversation(Long userId, Integer conversationType, Long targetId, 
                                          IMMessageEntity message, boolean incrementUnread) {
        if (userId == null) return;

        LambdaQueryWrapper<IMConversationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId)
               .eq(IMConversationEntity::getConversationType, conversationType)
               .eq(IMConversationEntity::getTargetId, targetId);
        
        IMConversationEntity conversation = this.getOne(wrapper);
        if (conversation == null) {
            conversation = new IMConversationEntity();
            conversation.setUserId(userId);
            conversation.setConversationType(conversationType);
            conversation.setTargetId(targetId);
            conversation.setUnreadCount(incrementUnread ? 1 : 0);
            conversation.setLastMsgSeq(message.getMsgSeq());
            conversation.setLastMsgContent(getContentDigest(message));
            conversation.setLastMsgTime(new Date(message.getMsgTime()));
            conversation.setIsTop(0);
            conversation.setIsMute(0);
            conversation.setCreateTime(new Date());
            conversation.setUpdateTime(new Date());
            this.save(conversation);
        } else {
            conversation.setLastMsgSeq(message.getMsgSeq());
            conversation.setLastMsgContent(getContentDigest(message));
            conversation.setLastMsgTime(new Date(message.getMsgTime()));
            if (incrementUnread) {
                conversation.setUnreadCount(conversation.getUnreadCount() + 1);
            }
            conversation.setUpdateTime(new Date());
            this.updateById(conversation);
        }
    }

    @Override
    public List<IMConversationEntity> getConversationList(Long userId) {
        LambdaQueryWrapper<IMConversationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId)
               .orderByDesc(IMConversationEntity::getLastMsgTime);
        return this.list(wrapper);
    }

    @Override
    public void clearUnread(Long userId, Integer conversationType, Long targetId) {
        LambdaUpdateWrapper<IMConversationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId)
               .eq(IMConversationEntity::getConversationType, conversationType)
               .eq(IMConversationEntity::getTargetId, targetId)
               .set(IMConversationEntity::getUnreadCount, 0);
        this.update(wrapper);
    }

    @Override
    public void deleteConversation(Long userId, Integer conversationType, Long targetId) {
        LambdaQueryWrapper<IMConversationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId)
               .eq(IMConversationEntity::getConversationType, conversationType)
               .eq(IMConversationEntity::getTargetId, targetId);
        this.remove(wrapper);
    }

    private String getContentDigest(IMMessageEntity message) {
        if (message.getContentType() == null) return "";
        switch (message.getContentType()) {
            case 1: // 文本
                String content = message.getContent();
                return content.length() > 50 ? content.substring(0, 47) + "..." : content;
            case 2: // 图片
                return "[图片]";
            case 3: // 语音
                return "[语音]";
            case 4: // 视频
                return "[视频]";
            case 5: // 文件
                return "[文件]";
            default:
                return "[未知消息]";
        }
    }
}
