package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM会话列表实体
 */
@Data
@TableName("t_im_conversation")
public class IMConversationEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
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
    
    private Date createTime;
    
    private Date updateTime;
}
