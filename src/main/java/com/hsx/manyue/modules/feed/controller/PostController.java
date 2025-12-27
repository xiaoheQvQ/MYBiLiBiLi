package com.hsx.manyue.modules.feed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.feed.model.entity.PostCommentEntity;
import com.hsx.manyue.modules.feed.model.entity.PostEntity;
import com.hsx.manyue.modules.feed.model.param.PostCommentParam;
import com.hsx.manyue.modules.feed.model.param.PostCreateParam;
import com.hsx.manyue.modules.feed.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
 
    private final IPostService postService;


    // 注入 RedisUtil 用于获取离线消息
    private final RedisUtil redisUtil;

    /**
     * 获取未读的评论通知
     */
    @GetMapping("/notifications/comment")
    public R getCommentNotifications() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String key = RedisKeys.POST_COMMENT_NOTIFICATIONS + userId;

        List<Object> notifications = redisUtil.lGet(key, 0, -1);

        if (notifications != null && !notifications.isEmpty()) {
            redisUtil.del(key);
        }

        return R.success(notifications);
    }

    /**
     * 获取未读的点赞通知
     */
    @GetMapping("/notifications/like")
    public R getLikeNotifications() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String key = RedisKeys.POST_LIKE_NOTIFICATIONS + userId;

        // 从Redis list中获取所有通知
        List<Object> notifications = redisUtil.lGet(key, 0, -1);

        // 获取后立即从Redis中删除，防止重复拉取
        if (notifications != null && !notifications.isEmpty()) {
            redisUtil.del(key);
        }

        return R.success(notifications);
    }
    /**
     * 图片上传接口
     */
    @PostMapping("/upload-image")
    public R uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = postService.uploadImage(file);
        return R.success(imageUrl);
    }

    @PostMapping
    public R createPost(@RequestBody PostCreateParam param) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(postService.createPost(param, userId));
    }
 
    @GetMapping("/feed")
    public R getPostFeed(Page<PostEntity> page) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        System.out.println("userId"+userId);
        return R.success(postService.getPostFeed(page, userId));
    }
 
    @PostMapping("/{postId}/like")
    public R likePost(@PathVariable Long postId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(postService.likePost(postId, userId));
    }
 
    @DeleteMapping("/{postId}/like")
    public R unlikePost(@PathVariable Long postId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(postService.unlikePost(postId, userId));
    }
 
    @PostMapping("/{postId}/comments")
    public R addComment(@PathVariable Long postId, @RequestBody PostCommentParam param) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        return R.success(postService.addComment(postId, param, userId));
    }
 
    @GetMapping("/{postId}/comments")
    public R getComments(@PathVariable Long postId, Page<PostCommentEntity> page) {
        return R.success(postService.getComments(postId, page));
    }
}