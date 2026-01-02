package com.hsx.manyue.modules.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.im.model.entity.IMGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM群组Mapper
 */
@Mapper
public interface IMGroupMapper extends BaseMapper<IMGroupEntity> {
    
    /**
     * 查询用户加入的群组列表
     */
    List<IMGroupEntity> queryUserGroups(@Param("userId") Long userId);
}
