package com.hsx.manyue.modules.feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.feed.model.entity.PostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
 

public interface PostMapper extends BaseMapper<PostEntity> {
 
    @Update("UPDATE t_post SET like_count = like_count + #{count} WHERE id = #{postId}")
    void updateLikeCount(@Param("postId") Long postId, @Param("count") int count);
 
    @Update("UPDATE t_post SET comment_count = comment_count + #{count} WHERE id = #{postId}")
    void updateCommentCount(@Param("postId") Long postId, @Param("count") int count);
}