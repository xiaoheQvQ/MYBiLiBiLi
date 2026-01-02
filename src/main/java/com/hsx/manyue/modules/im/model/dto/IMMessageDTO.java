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

    /**
     * @的用户ID列表
     */
    private java.util.List<Long> atUserIds;

    /**
     * 是否@所有人
     */
    private Boolean atAll;

    /**
     * 媒体文件URL(图片/语音/视频/文件)
     */
    private String mediaUrl;

    /**
     * 缩略图URL(图片/视频)
     */
    private String thumbnailUrl;

    /**
     * 时长(秒) - 用于语音/视频消息
     */
    private Integer duration;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件名称(原始文件名)
     */
    private String fileName;
}
