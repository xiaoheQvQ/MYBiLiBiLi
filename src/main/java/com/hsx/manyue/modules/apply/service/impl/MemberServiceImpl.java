package com.hsx.manyue.modules.apply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.apply.mapper.MemberPlanMapper;
import com.hsx.manyue.modules.apply.mapper.UserMemberMapper;
import com.hsx.manyue.modules.apply.model.entity.MemberPlanEntity;
import com.hsx.manyue.modules.apply.model.entity.UserMemberEntity;
import com.hsx.manyue.modules.apply.model.vo.MemberInfoVO;
import com.hsx.manyue.modules.apply.service.BalanceService;

import com.hsx.manyue.modules.apply.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberServiceImpl extends ServiceImpl<UserMemberMapper, UserMemberEntity> implements MemberService {

    @Autowired
    private MemberPlanMapper memberPlanMapper;
    
    @Autowired
    private BalanceService balanceService;

    @Override
    public List<MemberPlanEntity> getAvailablePlans() {
        return memberPlanMapper.selectAvailablePlans();
    }

    @Override
    public UserMemberEntity getUserMemberInfo(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public MemberInfoVO getMemberInfo(Long userId) {
        UserMemberEntity userMember = getUserMemberInfo(userId);
        MemberInfoVO vo = new MemberInfoVO();
        
        if (userMember != null) {
            vo.setUserId(userId);
            vo.setMemberLevel(userMember.getLevel());
            vo.setStartTime(userMember.getStartTime());
            vo.setEndTime(userMember.getEndTime());
            vo.setMemberStatus(userMember.getStatus());
            vo.setHasMember(true);
        } else {
            vo.setUserId(userId);
            vo.setMemberLevel(0);
            vo.setHasMember(false);
        }
        
        return vo;
    }

    @Override
    @Transactional
    public boolean purchaseMember(Long userId, Long planId, boolean useCoins) {
        MemberPlanEntity plan = memberPlanMapper.selectById(planId);
        if (plan == null || !plan.getStatus()) {
            throw new RuntimeException("套餐不存在或已下架");
        }
        
        UserMemberEntity currentMember = getUserMemberInfo(userId);
        if (currentMember != null && currentMember.getStatus()) {
            throw new RuntimeException("您已经是会员，请先取消或等待到期");
        }
        
        // 处理支付
        if (useCoins) {
            if (!balanceService.deductCoins(userId, plan.getCoinPrice(), "MEMBER_" + planId, "购买会员套餐: " + plan.getName())) {
                throw new RuntimeException("金币扣除失败");
            }
        }
        
        // 创建会员记录
        LocalDateTime now = LocalDateTime.now();
        UserMemberEntity newMember = new UserMemberEntity();
        newMember.setUserId(userId);
        newMember.setPlanId(planId);
        newMember.setLevel(plan.getLevel());
        newMember.setStartTime(now);
        newMember.setEndTime(now.plusDays(plan.getDuration()));
        newMember.setStatus(true);
        
        return this.save(newMember);
    }

    @Override
    public boolean checkMemberStatus(Long userId) {
        UserMemberEntity member = getUserMemberInfo(userId);
        return member != null && member.getStatus();
    }

    @Override
    @Transactional
    public boolean renewMember(Long userId, Long planId, boolean useCoins) {
        MemberPlanEntity plan = memberPlanMapper.selectById(planId);
        if (plan == null || !plan.getStatus()) {
            throw new RuntimeException("套餐不存在或已下架");
        }
        
        UserMemberEntity currentMember = getUserMemberInfo(userId);
        if (currentMember == null) {
            throw new RuntimeException("您还不是会员，请先购买");
        }
        
        // 处理支付
        if (useCoins) {
            if (!balanceService.deductCoins(userId, plan.getCoinPrice(), "MEMBER_RENEW_" + planId, "续费会员套餐: " + plan.getName())) {
                throw new RuntimeException("金币扣除失败");
            }
        }
        
        // 更新会员记录
        LocalDateTime newEndTime = currentMember.getEndTime().isBefore(LocalDateTime.now()) 
                ? LocalDateTime.now().plusDays(plan.getDuration())
                : currentMember.getEndTime().plusDays(plan.getDuration());
        
        currentMember.setPlanId(planId);
        currentMember.setLevel(plan.getLevel());
        currentMember.setEndTime(newEndTime);
        currentMember.setStatus(true);
        
        return this.updateById(currentMember);
    }
}