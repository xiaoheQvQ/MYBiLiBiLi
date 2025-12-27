package com.hsx.manyue.modules.apply.service;



import com.hsx.manyue.modules.apply.model.entity.MemberPlanEntity;
import com.hsx.manyue.modules.apply.model.entity.UserMemberEntity;
import com.hsx.manyue.modules.apply.model.vo.MemberInfoVO;

import java.util.List;

public interface MemberService {
    List<MemberPlanEntity> getAvailablePlans();
    
    UserMemberEntity getUserMemberInfo(Long userId);
    
    MemberInfoVO getMemberInfo(Long userId);
    
    boolean purchaseMember(Long userId, Long planId, boolean useCoins);
    
    boolean checkMemberStatus(Long userId);
    
    boolean renewMember(Long userId, Long planId, boolean useCoins);
}