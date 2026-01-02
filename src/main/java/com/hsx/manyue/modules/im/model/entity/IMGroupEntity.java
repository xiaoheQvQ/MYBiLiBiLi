package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM群组实体
 */
@Data
@TableName("t_im_group")
public class IMGroupEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 群组名称
     */
    private String groupName;
    
    /**
     * 群头像
     */
    private String groupAvatar;
    
    /**
     * 群主ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerId;
    
    /**
     * 群类型(1-普通群,2-超级群)
     */
    private Integer groupType;
    
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 最大成员数
     */
    private Integer maxMemberCount;
    
    /**
     * 群简介
     */
    private String introduction;
    
    /**
     * 群公告
     */
    private String notification;
    
    /**
     * 全员禁言
     */
    private Integer muteAll;
    
    /**
     * 状态(0-已解散,1-正常)
     */
    private Integer status;
    
    private Date createTime;
    
    private Date updateTime;
    
    private Integer isDelete;
}
