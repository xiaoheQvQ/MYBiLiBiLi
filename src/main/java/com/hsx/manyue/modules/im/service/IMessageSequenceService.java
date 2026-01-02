package com.hsx.manyue.modules.im.service;

/**
 * 消息序列号服务
 */
public interface IMessageSequenceService {
    
    /**
     * 获取下一个序列号
     * @param sessionType 会话类型(1-C2C,2-GROUP)
     * @param sessionId 会话ID
     * @return 序列号
     */
    Long nextSequence(Integer sessionType, String sessionId);
}
