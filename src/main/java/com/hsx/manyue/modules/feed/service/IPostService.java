package com.hsx.manyue.modules.feed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.feed.model.entity.PostCommentEntity;
import com.hsx.manyue.modules.feed.model.entity.PostEntity;
import com.hsx.manyue.modules.feed.model.param.PostCommentParam;
import com.hsx.manyue.modules.feed.model.param.PostCreateParam;
import com.hsx.manyue.modules.feed.model.vo.PostCommentVO;
import com.hsx.manyue.modules.feed.model.vo.PostVO;
import org.springframework.web.multipart.MultipartFile;

public interface IPostService extends IService<PostEntity> {
 
    // 发布动态
    boolean createPost(PostCreateParam param, Long userId);
 
    // 获取动态流
    Page<PostVO> getPostFeed(Page<PostEntity> page, Long currentUserId);
 
    // 点赞
    boolean likePost(Long postId, Long userId);
 
    // 取消点赞
    boolean unlikePost(Long postId, Long userId);
 
    // 发布评论
    PostCommentVO addComment(Long postId, PostCommentParam param, Long userId);
 
    // 获取评论列表
    Page<PostCommentVO> getComments(Long postId, Page<PostCommentEntity> page);

    /**
     * 处理图片上传并返回URL
     */
    String uploadImage(MultipartFile file);
}