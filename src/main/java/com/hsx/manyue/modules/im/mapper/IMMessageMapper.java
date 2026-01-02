package com.hsx.manyue.modules.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM消息Mapper
 */
@Mapper
public interface IMMessageMapper extends BaseMapper<IMMessageEntity> {
    
    /**
     * 查询历史消息
     */
    List<IMMessageEntity> queryHistory(@Param("userId") Long userId,
                                       @Param("targetId") Long targetId,
                                       @Param("sessionType") Integer sessionType,
                                       @Param("startSeq") Long startSeq,
                                       @Param("limit") Integer limit);
    
    /**
     * 统计未读消息数
     */
    Integer countUnread(@Param("userId") Long userId);
    
    /**
     * 同步消息(拉取lastSeq之后的所有消息)
     */
    List<IMMessageEntity> syncMessages(@Param("userId") Long userId,
                                       @Param("lastSeq") Long lastSeq);
}
