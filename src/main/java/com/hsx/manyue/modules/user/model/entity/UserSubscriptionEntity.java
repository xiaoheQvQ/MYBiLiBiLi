package com.hsx.manyue.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户订阅表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_user_subscription")
@Schema(description = "用户订阅表")
public class UserSubscriptionEntity extends BaseEntity<UserSubscriptionEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "被订阅用户ID")
    private Long authorId;

    @Override
    public Serializable pkVal() {
        return null;
    }
}
