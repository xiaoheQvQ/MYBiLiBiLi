package com.hsx.manyue.modules.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.im.model.entity.IMGroupMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM群成员Mapper
 */
@Mapper
public interface IMGroupMemberMapper extends BaseMapper<IMGroupMemberEntity> {
    
    /**
     * 查询群成员ID列表
     */
    List<Long> queryGroupMemberIds(@Param("groupId") Long groupId);
}
