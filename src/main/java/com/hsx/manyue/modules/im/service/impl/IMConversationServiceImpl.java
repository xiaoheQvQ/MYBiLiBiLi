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

import com.hsx.manyue.modules.im.service.IIMGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * IM会话服务实现
 */
@Slf4j
@Service
public class IMConversationServiceImpl extends ServiceImpl<IMConversationMapper, IMConversationEntity> 
        implements IIMConversationService {

    @Autowired
    @Lazy
    private IIMGroupService groupService;

    @Override
    @Transactional
    public void updateConversation(IMMessageEntity message) {
        updateConversation(message, null, null);
    }

    @Override
    @Transactional
    public void updateConversation(IMMessageEntity message, List<Long> atUserIds, Boolean atAll) {
        // 1. 更新接收者的会话
        if (message.getSessionType() == 1) { // 单聊
            updateOrCreateConversation(message.getToUserId(), 1, message.getFromUserId(), 
                    message, true, 0);
        } else { // 群聊
            // 获取所有群成员
            List<Long> memberIds = groupService.getGroupMemberIds(message.getToGroupId());
            
            for (Long memberId : memberIds) {
                if (memberId.equals(message.getFromUserId())) continue; // 发送者跳过增加未读数，但可能需要更新最后消息
                
                int atMeStatus = 0;
                if (Boolean.TRUE.equals(atAll)) {
                    atMeStatus = 2; // @所有人
                } else if (atUserIds != null && atUserIds.contains(memberId)) {
                    atMeStatus = 1; // @我
                }
                
                updateOrCreateConversation(memberId, 2, message.getToGroupId(), 
                        message, true, atMeStatus);
            }
        }
        
        // 2. 更新发送者的会话 (不增加未读数, 无@)
        updateOrCreateConversation(message.getFromUserId(), message.getSessionType(),
                message.getSessionType() == 1 ? message.getToUserId() : message.getToGroupId(), 
                message, false, 0);
    }

    @Override
    @Transactional
    public void updateOrCreateConversation(Long userId, Integer conversationType, Long targetId, 
                                          String lastMsgContent, Long lastMsgSeq) {
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
            conversation.setUnreadCount(0);
            conversation.setLastMsgSeq(lastMsgSeq);
            conversation.setLastMsgContent(lastMsgContent);
            conversation.setLastMsgTime(new Date());
            conversation.setIsTop(0);
            conversation.setIsMute(0);
            conversation.setCreateTime(new Date());
            conversation.setUpdateTime(new Date());
            this.save(conversation);
        } else {
            if (lastMsgSeq != null) {
                conversation.setLastMsgSeq(lastMsgSeq);
            }
            conversation.setLastMsgContent(lastMsgContent);
            conversation.setLastMsgTime(new Date());
            conversation.setUpdateTime(new Date());
            this.updateById(conversation);
        }
    }

    private void updateOrCreateConversation(Long userId, Integer conversationType, Long targetId, 
                                          IMMessageEntity message, boolean incrementUnread, int atMeStatus) {
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
            conversation.setAtMeStatus(atMeStatus);
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
            // 只有当新的@状态更高时才覆盖（或者按需更新，这里简单处理：如果有@就更新）
            if (atMeStatus > 0) {
                conversation.setAtMeStatus(atMeStatus);
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
               .set(IMConversationEntity::getUnreadCount, 0)
               .set(IMConversationEntity::getAtMeStatus, 0);
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
        
        String content = message.getContent();
        String textContent = "";
        String fileName = null;
        
        // 尝试解析JSON内容
        if (content != null && content.startsWith("{") && content.endsWith("}")) {
            try {
                cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(content);
                if (json.containsKey("text")) {
                    textContent = json.getStr("text");
                }
                if (json.containsKey("fileName")) {
                    fileName = json.getStr("fileName");
                }
            } catch (Exception e) {
                textContent = content;
            }
        } else {
            textContent = content;
        }
        
        switch (message.getContentType()) {
            case 1: // 文本
                if (textContent == null || textContent.isEmpty()) return "";
                return textContent.length() > 50 ? textContent.substring(0, 47) + "..." : textContent;
            case 2: // 图片
                return "[图片]";
            case 3: // 语音
                return "[语音]";
            case 4: // 视频
                return "[视频]";
            case 5: // 文件
                if (fileName != null && !fileName.isEmpty()) {
                    return "[文件] " + (fileName.length() > 20 ? fileName.substring(0, 17) + "..." : fileName);
                }
                return "[文件]";
            default:
                return "[未知消息]";
        }
    }

    @Override
    public void pinConversation(Long userId, Integer conversationType, Long targetId, Boolean isTop) {
        LambdaUpdateWrapper<IMConversationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId)
               .eq(IMConversationEntity::getConversationType, conversationType)
               .eq(IMConversationEntity::getTargetId, targetId)
               .set(IMConversationEntity::getIsTop, isTop ? 1 : 0);
        this.update(wrapper);
    }

    @Override
    public void muteConversation(Long userId, Integer conversationType, Long targetId, Boolean isMute) {
        LambdaUpdateWrapper<IMConversationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId)
               .eq(IMConversationEntity::getConversationType, conversationType)
               .eq(IMConversationEntity::getTargetId, targetId)
               .set(IMConversationEntity::getIsMute, isMute ? 1 : 0);
        this.update(wrapper);
    }
}
