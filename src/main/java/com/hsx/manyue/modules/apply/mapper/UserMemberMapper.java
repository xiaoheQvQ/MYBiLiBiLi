package com.hsx.manyue.modules.apply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.apply.model.entity.UserMemberEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface UserMemberMapper extends BaseMapper<UserMemberEntity> {
    @Select("SELECT * FROM t_user_member WHERE user_id = #{userId} AND is_delete = 0")
    UserMemberEntity selectByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE t_user_member SET status = 0 WHERE end_time < NOW() AND status = 1")
    int updateExpiredMembers();
}