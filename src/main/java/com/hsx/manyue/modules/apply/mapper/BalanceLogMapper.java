package com.hsx.manyue.modules.apply.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.apply.model.entity.BalanceLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface BalanceLogMapper extends BaseMapper<BalanceLog> {

    @Select("SELECT * FROM t_balance_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<BalanceLog> selectByUserId(@Param("userId") Long userId, 
                                   @Param("offset") Integer offset, 
                                   @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM t_balance_log WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM t_balance_log WHERE business_id = #{businessId} LIMIT 1")
    BalanceLog selectByBusinessId(@Param("businessId") String businessId);
}