package com.hsx.manyue.modules.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.im.model.entity.IMConversationEntity;
import com.hsx.manyue.modules.im.model.vo.IMConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM会话Mapper
 */
@Mapper
public interface IMConversationMapper extends BaseMapper<IMConversationEntity> {
    
    /**
     * 查询用户的会话列表（包含目标用户/群组信息）
     */
    List<IMConversationVO> queryConversationListWithInfo(@Param("userId") Long userId);
}
