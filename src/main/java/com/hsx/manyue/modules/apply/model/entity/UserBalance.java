package com.hsx.manyue.modules.apply.model.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import com.hsx.manyue.modules.user.model.entity.RefreshTokenEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_user_balance")
public class UserBalance extends BaseEntity<UserBalance> {

    private Long userId;
    private BigDecimal balance;
    private Integer coinBalance;

}