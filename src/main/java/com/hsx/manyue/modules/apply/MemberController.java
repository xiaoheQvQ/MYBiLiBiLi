package com.hsx.manyue.modules.apply;


import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.apply.model.entity.MemberPlanEntity;
import com.hsx.manyue.modules.apply.service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/plans")
    public R  getAvailablePlans() {
        return R.success(memberService.getAvailablePlans());
    }

    @GetMapping("/info")
    public R getMemberInfo() {

        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(memberService.getMemberInfo(userId));
    }

    @PostMapping("/purchase")
    public R purchaseMember(
                                        @RequestParam Long planId,
                                        @RequestParam(required = false, defaultValue = "false") boolean useCoins) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(memberService.purchaseMember(userId, planId, useCoins));
    }

    @PostMapping("/renew")
    public R renewMember(
                                      @RequestParam Long planId,
                                      @RequestParam(required = false, defaultValue = "false") boolean useCoins) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(memberService.renewMember(userId, planId, useCoins));
    }

    @GetMapping("/check")
    public R checkMemberStatus()
    {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(memberService.checkMemberStatus(userId));
    }
}