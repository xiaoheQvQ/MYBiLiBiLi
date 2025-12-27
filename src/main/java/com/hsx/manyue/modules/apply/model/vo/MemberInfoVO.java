package com.hsx.manyue.modules.apply.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "会员信息视图对象")
public class MemberInfoVO {
    
    @Schema(name = "用户ID")
    private Long userId;
    
    @Schema(name = "是否有会员")
    private Boolean hasMember;
    
    @Schema(name = "会员等级 1-普通 2-高级 3-尊享")
    private Integer memberLevel;
    
    @Schema(name = "会员开始时间")
    private LocalDateTime startTime;
    
    @Schema(name = "会员结束时间")
    private LocalDateTime endTime;
    
    @Schema(name = "会员状态 true-有效 false-已过期")
    private Boolean memberStatus;
    
    @Schema(name = "会员套餐名称")
    private String planName;
    
    @Schema(name = "剩余天数")
    private Integer remainingDays;
    
    @Schema(name = "会员图标URL")
    private String memberIcon;
    
    @Schema(name = "是否即将过期(7天内)")
    private Boolean expireSoon;
    
    // 可以添加一些计算字段的方法
    public Integer getRemainingDays() {
        if (endTime == null || !hasMember) {
            return 0;
        }
        return (int) java.time.Duration.between(LocalDateTime.now(), endTime).toDays();
    }
    
    public Boolean getExpireSoon() {
        if (endTime == null || !hasMember) {
            return false;
        }
        return getRemainingDays() <= 7;
    }
    
    // 获取会员等级名称
    public String getLevelName() {
        switch (memberLevel) {
            case 1: return "普通会员";
            case 2: return "高级会员";
            case 3: return "尊享会员";
            default: return "非会员";
        }
    }
}