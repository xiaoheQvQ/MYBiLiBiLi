package com.hsx.manyue.modules.im.model.dto;

import lombok.Data;

/**
 * IM消息DTO
 */
@Data
public class IMMessageDTO {
    
    /**
     * 客户端消息ID(去重)
     */
    private String clientMsgId;
    
    /**
     * 发送者ID
     */
    private Long fromUserId;
    
    /**
     * 接收者ID(单聊)
     */
    private Long toUserId;
    
    /**
     * 群组ID(群聊)
     */
    private Long toGroupId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 内容类型(1-文本,2-图片,3-语音,4-视频,5-文件)
     */
    private Integer contentType;
}
