package com.hsx.manyue.modules.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.im.model.entity.IMMessageSequenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * IM消息序列号Mapper
 */
@Mapper
public interface IMMessageSequenceMapper extends BaseMapper<IMMessageSequenceEntity> {
    
    /**
     * 获取并递增序列号(原子操作)
     */
    Long getAndIncrementSeq(@Param("sessionType") Integer sessionType,
                           @Param("sessionId") String sessionId);
}
