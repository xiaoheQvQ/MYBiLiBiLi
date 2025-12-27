package com.hsx.manyue.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.model.entity.UserSubscriptionEntity;

import java.util.List;

/**
 * 用户订阅表 Mapper 接口
 */
public interface UserSubscriptionMapper extends BaseMapper<UserSubscriptionEntity> {

    List<UserDTO> getSubscriptions(Long userId);

    List<UserDTO> getSubscribedList(Long userId);
}
