package com.hsx.manyue.modules.apply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.hsx.manyue.modules.apply.model.entity.UserBalance;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

public interface UserBalanceMapper extends BaseMapper<UserBalance> {

    @Select("SELECT * FROM t_user_balance WHERE user_id = #{userId} LIMIT 1")
    UserBalance selectByUserId(@Param("userId") Long userId);

    @Update("UPDATE t_user_balance SET " +
            "balance = balance + #{amount}, " +
            "coin_balance = coin_balance + #{coins}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int increaseBalance(@Param("userId") Long userId, 
                       @Param("amount") BigDecimal amount, 
                       @Param("coins") Integer coins);

    @Update("UPDATE t_user_balance SET " +
            "balance = balance - #{amount}, " +
            "coin_balance = coin_balance - #{coins}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId} " +
            "AND balance >= #{amount} " +
            "AND coin_balance >= #{coins}")
    int decreaseBalance(@Param("userId") Long userId, 
                       @Param("amount") BigDecimal amount, 
                       @Param("coins") Integer coins);

    @Update("UPDATE t_user_balance SET " +
            "coin_balance = coin_balance + #{coins}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int increaseCoins(@Param("userId") Long userId, 
                     @Param("coins") Integer coins);

    @Update("UPDATE t_user_balance SET " +
            "coin_balance = coin_balance - #{coins}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId} " +
            "AND coin_balance >= #{coins}")
    int decreaseCoins(@Param("userId") Long userId, 
                     @Param("coins") Integer coins);
}