package com.hsx.manyue.modules.im.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM会话列表VO（包含目标用户/群组信息）
 */
@Data
public class IMConversationVO {
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    /**
     * 会话类型(1-单聊,2-群聊)
     */
    private Integer conversationType;
    
    /**
     * 目标ID(userId或groupId)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    
    /**
     * 目标名称（用户昵称或群组名称）
     */
    private String targetName;
    
    /**
     * 目标头像（用户头像或群组头像）
     */
    private String targetAvatar;
    
    /**
     * 群组名称（仅群聊时有值）
     */
    private String groupName;
    
    /**
     * 群组头像（仅群聊时有值）
     */
    private String groupAvatar;
    
    /**
     * 最后消息序列号
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastMsgSeq;
    
    /**
     * 最后消息内容摘要
     */
    private String lastMsgContent;
    
    /**
     * 最后消息时间
     */
    private Date lastMsgTime;
    
    /**
     * 未读数
     */
    private Integer unreadCount;
    
    /**
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 是否免打扰
     */
    private Integer isMute;
    
    /**
     * @我状态(0-无@, 1-有@我, 2-有@所有人)
     */
    private Integer atMeStatus;
    
    private Date createTime;
    
    private Date updateTime;
}
