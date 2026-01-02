package com.hsx.manyue.modules.im.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.im.mapper.IMMessageMapper;
import com.hsx.manyue.modules.im.model.IMMessage;
import com.hsx.manyue.modules.im.model.dto.IMMessageDTO;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import com.hsx.manyue.modules.im.server.SessionManager;
import com.hsx.manyue.modules.im.service.IIMConversationService;
import com.hsx.manyue.modules.im.service.IIMGroupService;
import com.hsx.manyue.modules.im.service.IIMMessageService;
import com.hsx.manyue.modules.im.service.IMessageSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * IM消息服务实现
 * 
 * 设计原则:
 * 1. 消息先实时推送,异步持久化到DB (保证实时性)
 * 2. 消息序列号保证有序性
 * 3. clientMsgId保证幂等性
 * 4. ACK机制保证可靠性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IMMessageServiceImpl extends ServiceImpl<IMMessageMapper, IMMessageEntity> 
        implements IIMMessageService {

    private final IMessageSequenceService sequenceService;
    private final IIMConversationService conversationService;
    private final IIMGroupService groupService;
    private final SessionManager sessionManager;
    private final RedisUtil redisUtil;
    
    private static final String DEDUP_KEY_PREFIX = "im:dedup:";
    private static final int DEDUP_EXPIRE_SECONDS = 86400; // 24小时

    @Override
    @Transactional
    public IMMessageEntity sendSingleMessage(IMMessageDTO dto) {
        // 0. 参数验证
        if (dto.getFromUserId() == null || dto.getToUserId() == null) {
            log.error("消息参数错误: fromUserId={}, toUserId={}", dto.getFromUserId(), dto.getToUserId());
            throw new IllegalArgumentException("发送者ID和接收者ID不能为空");
        }
        
        // 1. 幂等性检查
        if (isDuplicate(dto.getClientMsgId())) {
            log.warn("重复消息: clientMsgId={}", dto.getClientMsgId());
            return null;
        }
        
        // 2. 生成全局唯一序列号
        String sessionId = buildSessionId(dto.getFromUserId(), dto.getToUserId());
        Long msgSeq = sequenceService.nextSequence(1, sessionId);
        
        // 3. 构建消息实体
        IMMessageEntity message = buildMessageEntity(dto, msgSeq, (int) IMMessage.TYPE_SINGLE_CHAT, 1);
        
        // 4. 持久化到数据库
        this.save(message);
        
        // 5. 更新会话列表 (必须在推送前，确保对方收到消息后拉取列表能看到最新条目)
        conversationService.updateConversation(message);
        
        // 6. 实时推送给接收者 (如果在线)
        IMMessage imMessage = convertToIMMessage(message);
        sessionManager.pushMessage(dto.getToUserId(), imMessage);
        
        // 7. 发送ACK给发送者
        IMMessage ack = IMMessage.createAck(msgSeq, dto.getFromUserId());
        sessionManager.pushMessage(dto.getFromUserId(), ack);
        
        // 8. 标记消息已去重
        markAsProcessed(dto.getClientMsgId());
        
        log.info("单聊消息发送成功: msgSeq={}, from={}, to={}", msgSeq, dto.getFromUserId(), dto.getToUserId());
        return message;
    }

    @Override
    @Transactional
    public IMMessageEntity sendGroupMessage(IMMessageDTO dto) {
        // 1. 幂等性检查
        if (isDuplicate(dto.getClientMsgId())) {
            log.warn("重复消息: clientMsgId={}", dto.getClientMsgId());
            return null;
        }
        
        // 2. 生成群消息序列号
        Long msgSeq = sequenceService.nextSequence(2, String.valueOf(dto.getToGroupId()));
        
        // 3. 构建消息实体
        IMMessageEntity message = buildMessageEntity(dto, msgSeq, (int) IMMessage.TYPE_GROUP_CHAT, 2);
        
        // 4. 持久化到数据库
        this.save(message);
        
        // 5. 更新会话列表
        conversationService.updateConversation(message);
        
        // 6. 推送给所有在线群成员
        IMMessage imMessage = convertToIMMessage(message);
        List<Long> memberIds = groupService.getGroupMemberIds(dto.getToGroupId());
        sessionManager.pushGroupMessage(dto.getToGroupId(), imMessage, memberIds);

        // 7. 发送ACK给发送者
        IMMessage ack = IMMessage.createAck(msgSeq, dto.getFromUserId());
        sessionManager.pushMessage(dto.getFromUserId(), ack);
        
        // 8. 标记消息已去重
        markAsProcessed(dto.getClientMsgId());
        
        log.info("群聊消息发送成功: msgSeq={}, from={}, groupId={}", msgSeq, dto.getFromUserId(), dto.getToGroupId());
        return message;
    }

    @Override
    public void ackMessage(Long msgSeq, Long userId) {
        // 更新消息状态为已送达
        LambdaQueryWrapper<IMMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMMessageEntity::getMsgSeq, msgSeq);
        
        IMMessageEntity entity = this.getOne(wrapper);
        if (entity != null && entity.getStatus() == 0) {
            entity.setStatus(1); // 已送达
            this.updateById(entity);
            log.info("消息ACK: msgSeq={}, userId={}", msgSeq, userId);
        }
    }

    @Override
    public void readMessage(Long msgSeq, Long userId) {
        // 标记消息已读
        LambdaQueryWrapper<IMMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMMessageEntity::getMsgSeq, msgSeq);
        
        IMMessageEntity entity = this.getOne(wrapper);
        if (entity != null) {
            entity.setStatus(2); // 已读
            this.updateById(entity);
            
            // 发送已读回执给发送者
            IMMessage receipt = new IMMessage();
            receipt.setMsgType(IMMessage.TYPE_READ_RECEIPT);
            receipt.setMsgSeq(msgSeq);
            receipt.setFromUserId(userId);
            receipt.setMsgTime(System.currentTimeMillis());
            sessionManager.pushMessage(entity.getFromUserId(), receipt);
            
            log.info("消息已读: msgSeq={}, userId={}", msgSeq, userId);
        }
    }

    @Override
    public List<IMMessageEntity> pullHistory(Long userId, Long targetId, Integer sessionType, 
                                              Long startSeq, Integer limit) {
        // 从DB拉取历史消息
        return baseMapper.queryHistory(userId, targetId, sessionType, startSeq, limit);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        // 统计未读消息数
        return baseMapper.countUnread(userId);
    }

    @Override
    public List<IMMessageEntity> syncMessages(Long userId, Long lastSeq) {
        // 增量同步 (拉取lastSeq之后的所有消息)
        return baseMapper.syncMessages(userId, lastSeq);
    }

    @Override
    public boolean isDuplicate(String clientMsgId) {
        if (clientMsgId == null) {
            return false;
        }
        String key = DEDUP_KEY_PREFIX + clientMsgId;
        return redisUtil.hasKey(key);
    }

    /**
     * 标记消息已处理(去重)
     */
    private void markAsProcessed(String clientMsgId) {
        if (clientMsgId != null) {
            String key = DEDUP_KEY_PREFIX + clientMsgId;
            redisUtil.set(key, "1", DEDUP_EXPIRE_SECONDS);
        }
    }

    /**
     * 构建会话ID
     */
    private String buildSessionId(Long userId1, Long userId2) {
        // 参数验证
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("userId1 和 userId2 不能为空");
        }
        
        // 确保会话ID唯一性: 小ID_大ID
        if (userId1 < userId2) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    /**
     * 构建消息实体
     */
    private IMMessageEntity buildMessageEntity(IMMessageDTO dto, Long msgSeq, Integer msgType, Integer sessionType) {
        IMMessageEntity entity = new IMMessageEntity();
        entity.setId(IdUtil.getSnowflakeNextId());
        entity.setMsgSeq(msgSeq);
        entity.setMsgType(msgType);
        entity.setSessionType(sessionType);
        entity.setFromUserId(dto.getFromUserId());
        entity.setToUserId(dto.getToUserId());
        entity.setToGroupId(dto.getToGroupId());
        entity.setContent(dto.getContent());
        entity.setContentType(dto.getContentType());
        entity.setClientMsgId(dto.getClientMsgId());
        entity.setMsgTime(System.currentTimeMillis());
        entity.setStatus(0); // 发送中
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setIsDelete(0);
        return entity;
    }

    /**
     * 转换为IM消息
     */
    private IMMessage convertToIMMessage(IMMessageEntity entity) {
        IMMessage message = new IMMessage();
        message.setMsgType(entity.getMsgType().byteValue());
        message.setMsgSeq(entity.getMsgSeq());
        message.setFromUserId(entity.getFromUserId());
        message.setToUserId(entity.getToUserId());
        message.setToGroupId(entity.getToGroupId());
        message.setContent(entity.getContent());
        message.setContentType(entity.getContentType());
        message.setMsgTime(entity.getMsgTime());
        message.setClientMsgId(entity.getClientMsgId());
        return message;
    }
}
