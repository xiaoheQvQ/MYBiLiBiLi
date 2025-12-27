package com.hsx.manyue.modules.apply;

import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.apply.model.entity.BalanceLog;
import com.hsx.manyue.modules.apply.model.entity.UserBalance;
import com.hsx.manyue.modules.apply.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/balance")
public class BalanceController {
    
    @Autowired
    private BalanceService balanceService;
    
    @GetMapping("/info")
    public R getBalanceInfo() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        UserBalance balance = balanceService.getUserBalance(userId);
        return R.success(balance);
    }

    @PostMapping("/recharge")
    public R recharge(
                      @RequestParam BigDecimal amount,
                      @RequestParam String orderId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        boolean result = balanceService.recharge(userId, amount, orderId);
        return result ? R.success("充值成功") : R.failure("充值失败");
    }

    @PostMapping("/consume")
    public R consumeCoins(
                          @RequestParam Integer coins,
                          @RequestParam String businessId,
                          @RequestParam(required = false) String remark) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        try {
            boolean result = balanceService.consumeCoins(userId, coins, businessId, remark);
            return result ? R.success("消费成功") : R.failure("消费失败");
        } catch (Exception e) {
            return R.failure(e.getMessage());
        }
    }

    @GetMapping("/logs")
    public R getBalanceLogs(
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<BalanceLog> logs = balanceService.getBalanceLogs(userId, page, size);
        return R.success(logs);
    }
}