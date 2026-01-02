package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM消息实体
 */
@Data
@TableName("t_im_message")
public class IMMessageEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 消息序列号(全局唯一)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long msgSeq;
    
    /**
     * 消息类型(1-单聊,2-群聊,3-系统)
     */
    private Integer msgType;
    
    /**
     * 会话类型(1-C2C,2-GROUP)
     */
    private Integer sessionType;
    
    /**
     * 发送者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromUserId;
    
    /**
     * 接收者ID(单聊)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;
    
    /**
     * 群组ID(群聊)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toGroupId;
    
    /**
     * 消息内容(JSON格式)
     */
    private String content;
    
    /**
     * 内容类型(1-文本,2-图片,3-语音,4-视频,5-文件)
     */
    private Integer contentType;
    
    /**
     * 消息状态(0-发送中,1-已送达,2-已读)
     */
    private Integer status;
    
    /**
     * 消息时间戳(毫秒)
     */
    private Long msgTime;
    
    /**
     * 客户端消息ID(去重)
     */
    private String clientMsgId;
    
    private Date createTime;
    
    private Date updateTime;
    
    private Integer isDelete;
}
