package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM消息序列号实体
 */
@Data
@TableName("t_im_message_sequence")
public class IMMessageSequenceEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 会话类型(1-C2C,2-GROUP)
     */
    private Integer sessionType;
    
    /**
     * 会话ID(userId_userId或groupId)
     */
    private String sessionId;
    
    /**
     * 当前最大序列号
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long maxSeq;
    
    private Date createTime;
    
    private Date updateTime;
}
