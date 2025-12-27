package com.hsx.manyue.modules.apply.service;

import com.hsx.manyue.modules.apply.model.entity.BalanceLog;
import com.hsx.manyue.modules.apply.model.entity.UserBalance;

import java.math.BigDecimal;
import java.util.List;

// BalanceService.java
public interface BalanceService {

    boolean deductCoins(Long userId, Integer coins, String businessId, String remark);

    boolean addCoins(Long userId, Integer coins, String businessId, String remark);

    /**
     * 获取用户余额
     */
    UserBalance getUserBalance(Long userId);
    
    /**
     * 充值余额
     */
    boolean recharge(Long userId, BigDecimal amount, String orderId);
    
    /**
     * 消费金币
     */
    boolean consumeCoins(Long userId, Integer coins, String businessId, String remark);
    
    /**
     * 退款
     */
    boolean refund(Long userId, Integer coins, String businessId, String remark);
    
    /**
     * 获取余额变动记录
     */
    List<BalanceLog> getBalanceLogs(Long userId, Integer page, Integer size);
}