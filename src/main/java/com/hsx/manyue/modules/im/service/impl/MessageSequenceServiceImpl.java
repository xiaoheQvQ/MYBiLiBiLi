package com.hsx.manyue.modules.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hsx.manyue.modules.im.mapper.IMMessageSequenceMapper;
import com.hsx.manyue.modules.im.model.entity.IMMessageSequenceEntity;
import com.hsx.manyue.modules.im.service.IMessageSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息序列号服务实现
 * 使用数据库乐观锁保证序列号的唯一性和连续性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSequenceServiceImpl implements IMessageSequenceService {

    private final IMMessageSequenceMapper sequenceMapper;

    @Override
    @Transactional
    public Long nextSequence(Integer sessionType, String sessionId) {
        // 查询当前序列号记录
        LambdaQueryWrapper<IMMessageSequenceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMMessageSequenceEntity::getSessionType, sessionType)
               .eq(IMMessageSequenceEntity::getSessionId, sessionId);
        
        IMMessageSequenceEntity entity = sequenceMapper.selectOne(wrapper);
        
        if (entity == null) {
            // 首次创建序列号记录
            entity = new IMMessageSequenceEntity();
            entity.setSessionType(sessionType);
            entity.setSessionId(sessionId);
            entity.setMaxSeq(1L);
            sequenceMapper.insert(entity);
            return 1L;
        } else {
            // 递增序列号
            Long nextSeq = entity.getMaxSeq() + 1;
            entity.setMaxSeq(nextSeq);
            sequenceMapper.updateById(entity);
            return nextSeq;
        }
    }
}
