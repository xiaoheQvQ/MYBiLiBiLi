package com.hsx.manyue.modules.chat.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.chat.mapper.ChatMessageMapper;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.chat.service.IChatMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@RequiredArgsConstructor
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    private final com.hsx.manyue.modules.chat.mq.ChatMessageMQProducer chatMessageMQProducer;

    @Override
    @Transactional
    public ChatMessage savePrivateMessage(Long fromUserId, Long toUserId, String content) {
        ChatMessage message = new ChatMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setStatus(0); // 默认未读
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        
        // 【优化】改为 MQ 异步处理
        try {
            chatMessageMQProducer.sendPrivateMessage(message);
            log.info("私信发送成功，正在处理中...");
        } catch (Exception e) {
            log.error("私信发送失败，降级为同步处理", e);
            this.save(message);
        }
        
        return message;
    }

    @Override
    public List<ChatMessage> getChatHistory(Long fromUserId, Long toUserId, int limit) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        
        // 查询双方之间的聊天记录
        queryWrapper.and(wrapper -> wrapper
                .eq("from_user_id", fromUserId)
                .eq("to_user_id", toUserId)
            )
            .or(wrapper -> wrapper
                .eq("from_user_id", toUserId)
                .eq("to_user_id", fromUserId)
            )
            .orderByDesc("create_time")
            .last(limit > 0 ? "LIMIT " + limit : "");
        
        return this.list(queryWrapper);
    }

    @Override
    public boolean updateMessagesStatus(Long fromUserId, Long toUserId, Integer status) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_user_id", fromUserId)
                   .eq("to_user_id", toUserId)
                   .eq("status", 0); // 只更新未读消息
        
        ChatMessage updateEntity = new ChatMessage();
        updateEntity.setStatus(status);
        updateEntity.setUpdateTime(new Date());
        
        return this.update(updateEntity, queryWrapper);
    }

    @Override
    public Long countUnreadMessages(Long userId) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_user_id", userId)
                   .eq("status", 0); // 未读消息
        
        return this.count(queryWrapper);
    }



}