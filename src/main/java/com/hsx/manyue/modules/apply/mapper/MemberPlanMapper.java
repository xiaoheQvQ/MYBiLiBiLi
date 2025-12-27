package com.hsx.manyue.modules.apply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.hsx.manyue.modules.apply.model.entity.MemberPlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface MemberPlanMapper extends BaseMapper<MemberPlanEntity> {
    @Select("SELECT * FROM t_member_plan WHERE status = 1 AND is_delete = 0 ORDER BY level, price")
    List<MemberPlanEntity> selectAvailablePlans();
}