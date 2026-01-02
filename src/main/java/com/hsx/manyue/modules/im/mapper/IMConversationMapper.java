package com.hsx.manyue.modules.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.im.model.entity.IMConversationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * IM会话Mapper
 */
@Mapper
public interface IMConversationMapper extends BaseMapper<IMConversationEntity> {
}
