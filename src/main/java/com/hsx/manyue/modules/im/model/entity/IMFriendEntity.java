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
 * IM好友关系实体
 */
@Data
@TableName("t_im_friend")
public class IMFriendEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    /**
     * 好友ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long friendId;
    
    /**
     * 备注名
     */
    private String remark;
    
    /**
     * 状态(1-正常,2-拉黑)
     */
    private Integer status;
    
    private Date createTime;
    
    private Date updateTime;
    
    private Integer isDelete;

    /**
     * 好友昵称
     */
    @TableField(exist = false)
    private String nick;

    /**
     * 好友头像
     */
    @TableField(exist = false)
    private String avatar;
}
