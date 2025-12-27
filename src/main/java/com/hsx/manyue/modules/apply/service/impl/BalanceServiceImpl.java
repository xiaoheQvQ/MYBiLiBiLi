package com.hsx.manyue.modules.apply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.apply.mapper.BalanceLogMapper;
import com.hsx.manyue.modules.apply.mapper.UserBalanceMapper;
import com.hsx.manyue.modules.apply.model.entity.BalanceLog;
import com.hsx.manyue.modules.apply.model.entity.UserBalance;
import com.hsx.manyue.modules.apply.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceServiceImpl extends ServiceImpl<UserBalanceMapper, UserBalance> implements BalanceService {
    
    @Autowired
    private BalanceLogMapper balanceLogMapper;

    @Override
    @Transactional
    public boolean deductCoins(Long userId, Integer coins, String businessId, String remark) {
        if (coins <= 0) {
            throw new RuntimeException("扣除金币必须大于0");
        }

        // 检查是否已处理过该业务
        BalanceLog existLog = balanceLogMapper.selectByBusinessId(businessId);
        if (existLog != null) {
            throw new RuntimeException("该业务已处理，请勿重复操作");
        }

        UserBalance balance = this.getUserBalance(userId);
        if (balance.getCoinBalance() < coins) {
            throw new RuntimeException("金币余额不足");
        }

        // 更新余额
        balance.setCoinBalance(balance.getCoinBalance() - coins);
        this.updateById(balance);

        // 记录日志
        BalanceLog log = new BalanceLog();
        log.setUserId(userId);
        log.setChangeAmount(BigDecimal.ZERO);
        log.setChangeCoins(-coins);
        log.setBalanceType(2); // 消费
        log.setBusinessId(businessId);
        log.setRemark(remark);
        balanceLogMapper.insert(log);

        return true;
    }

    @Override
    @Transactional
    public boolean addCoins(Long userId, Integer coins, String businessId, String remark) {
        if (coins <= 0) {
            throw new RuntimeException("增加金币必须大于0");
        }

        // 检查是否已处理过该业务
        BalanceLog existLog = balanceLogMapper.selectByBusinessId(businessId);
        if (existLog != null) {
            throw new RuntimeException("该业务已处理，请勿重复操作");
        }

        UserBalance balance = this.getUserBalance(userId);

        // 更新余额
        balance.setCoinBalance(balance.getCoinBalance() + coins);
        this.updateById(balance);

        // 记录日志
        BalanceLog log = new BalanceLog();
        log.setUserId(userId);
        log.setChangeAmount(BigDecimal.ZERO);
        log.setChangeCoins(coins);
        log.setBalanceType(4); // 赠送
        log.setBusinessId(businessId);
        log.setRemark(remark);
        balanceLogMapper.insert(log);

        return true;
    }

    @Override
    public UserBalance getUserBalance(Long userId) {
        LambdaQueryWrapper<UserBalance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBalance::getUserId, userId);
        UserBalance balance = this.getOne(queryWrapper);
        
        if (balance == null) {
            // 初始化用户余额
            balance = new UserBalance();
            balance.setUserId(userId);
            balance.setBalance(BigDecimal.ZERO);
            balance.setCoinBalance(0);
            this.save(balance);
        }
        
        return balance;
    }
    
    @Override
    @Transactional
    public boolean recharge(Long userId, BigDecimal amount, String orderId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        int coins = amount.multiply(new BigDecimal(10)).intValue();
        

        UserBalance balance = this.getUserBalance(userId);
        balance.setBalance(balance.getBalance().add(amount));
        balance.setCoinBalance(balance.getCoinBalance() + coins);
        this.updateById(balance);
        

        BalanceLog log = new BalanceLog();
        log.setUserId(userId);
        log.setChangeAmount(amount);
        log.setChangeCoins(coins);
        log.setBalanceType(1); // 充值
        log.setBusinessId(orderId);
        log.setRemark("充值金额: " + amount + "元");
        balanceLogMapper.insert(log);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean consumeCoins(Long userId, Integer coins, String businessId, String remark) {

        BalanceLog existLog = balanceLogMapper.selectByBusinessId(businessId);
        if (existLog != null) {
            throw new RuntimeException("该业务已处理，请勿重复操作");
        }

        if (coins <= 0) {
            throw new RuntimeException("消费金币必须大于0");
        }
        
        UserBalance balance = this.getUserBalance(userId);
        if (balance.getCoinBalance() < coins) {
            throw new RuntimeException("金币余额不足");
        }

        balance.setCoinBalance(balance.getCoinBalance() - coins);
        balance.setBalance(balance.getBalance().subtract(
                BigDecimal.valueOf(coins).divide(BigDecimal.TEN)
        ));
        this.updateById(balance);
        
        // 记录日志
        BalanceLog log = new BalanceLog();
        log.setUserId(userId);
        log.setChangeAmount(BigDecimal.ZERO);
        log.setChangeCoins(-coins);
        log.setBalanceType(2); // 消费
        log.setBusinessId(businessId);
        log.setRemark(remark);
        log.setChangeAmount(BigDecimal.valueOf(coins).divide(BigDecimal.TEN));
        balanceLogMapper.insert(log);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean refund(Long userId, Integer coins, String businessId, String remark) {
        if (coins <= 0) {
            throw new RuntimeException("退款金币必须大于0");
        }
        
        UserBalance balance = this.getUserBalance(userId);
        
        // 更新余额
        balance.setCoinBalance(balance.getCoinBalance() + coins);
        this.updateById(balance);
        
        // 记录日志
        BalanceLog log = new BalanceLog();
        log.setUserId(userId);
        log.setChangeAmount(BigDecimal.ZERO);
        log.setChangeCoins(coins);
        log.setBalanceType(3); // 退款
        log.setBusinessId(businessId);
        log.setRemark(remark);
        balanceLogMapper.insert(log);
        
        return true;
    }

    @Override
    public List<BalanceLog> getBalanceLogs(Long userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return balanceLogMapper.selectByUserId(userId, offset, size);
    }
}