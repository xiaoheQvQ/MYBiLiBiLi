package com.hsx.manyue.modules.apply.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_member")
public class UserMemberEntity extends BaseEntity<UserMemberEntity> {
    private Long userId;
    private Long planId;
    private Integer level;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean status;
}