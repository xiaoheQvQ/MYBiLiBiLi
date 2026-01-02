package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM用户在线状态实体
 */
@Data
@TableName("t_im_user_status")
public class IMUserStatusEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    /**
     * 状态(0-离线,1-在线,2-忙碌,3-离开)
     */
    private Integer status;
    
    /**
     * 平台(1-Web,2-Android,3-iOS,4-PC)
     */
    private Integer platform;
    
    /**
     * 客户端类型
     */
    private String clientType;
    
    /**
     * 最后心跳时间
     */
    private Date lastHeartbeatTime;
    
    /**
     * 登录时间
     */
    private Date loginTime;
    
    /**
     * 登出时间
     */
    private Date logoutTime;
    
    /**
     * 是否在线
     */
    private Integer isOnline;
    
    private Date createTime;
    
    private Date updateTime;
}
