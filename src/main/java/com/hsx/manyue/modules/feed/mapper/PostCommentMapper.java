package com.hsx.manyue.modules.feed.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsx.manyue.modules.feed.model.entity.PostCommentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface PostCommentMapper extends BaseMapper<PostCommentEntity> {
    /**
     * 分页查询根评论ID
     */
    Page<Long> findRootCommentIds(Page<?> page, @Param("postId") Long postId);

    /**
     * 根据根评论ID列表，查询所有相关的评论（包括根和所有子孙）
     */
    List<PostCommentEntity> findAllCommentsByRootIds(@Param("rootIds") List<Long> rootIds);
}