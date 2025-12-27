package com.hsx.manyue.modules.apply.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_balance_log")
public class BalanceLog  extends BaseEntity<BalanceLog> {

    private Long userId;
    private BigDecimal changeAmount;
    private Integer changeCoins;
    private Integer balanceType;
    private String businessId;
    private String remark;

}