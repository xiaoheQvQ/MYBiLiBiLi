package com.hsx.manyue.modules.im.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * IM群成员实体
 */
@Data
@TableName("t_im_group_member")
public class IMGroupMemberEntity {
    
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 群组ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long groupId;
    
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    /**
     * 角色(1-群主,2-管理员,3-普通成员)
     */
    private Integer memberRole;
    
    /**
     * 群昵称
     */
    private String nickName;
    
    /**
     * 加入时间
     */
    private Date joinTime;
    
    /**
     * 禁言结束时间
     */
    private Date muteEndTime;
    
    private Integer isDelete;

    /**
     * 用户昵称
     */
    @TableField(exist = false)
    private String nick;

    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String avatar;
}
