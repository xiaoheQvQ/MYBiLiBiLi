package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM好友申请实体
 */
@Data
@TableName("t_im_friend_apply")
public class IMFriendApplyEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 申请人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromUserId;
    
    /**
     * 被申请人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;
    
    /**
     * 申请消息
     */
    private String applyMsg;
    
    /**
     * 状态(0-待处理,1-已同意,2-已拒绝)
     */
    private Integer status;
    
    private Date createTime;
    
    private Date updateTime;
    /**
     * 申请人昵称
     */
    @TableField(exist = false)
    private String nick;

    /**
     * 申请人头像
     */
    @TableField(exist = false)
    private String avatar;
}
