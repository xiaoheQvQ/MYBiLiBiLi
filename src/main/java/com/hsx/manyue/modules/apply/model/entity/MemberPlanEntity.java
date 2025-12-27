package com.hsx.manyue.modules.apply.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_member_plan")
public class MemberPlanEntity extends BaseEntity<MemberPlanEntity> {
    private String name;
    private String description;
    private Integer duration;
    private BigDecimal price;
    private Integer coinPrice;
    private BigDecimal discount;
    private String icon;
    private Integer level;
    private Boolean status;
}