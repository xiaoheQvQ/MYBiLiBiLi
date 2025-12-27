package com.hsx.manyue.modules.feed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.feed.mapper.*;
import com.hsx.manyue.modules.feed.model.entity.*;
import com.hsx.manyue.modules.feed.model.param.PostCommentParam;
import com.hsx.manyue.modules.feed.model.param.PostCreateParam;
import com.hsx.manyue.modules.feed.model.vo.PostCommentVO;
import com.hsx.manyue.modules.feed.model.vo.PostVO;
import com.hsx.manyue.modules.feed.model.vo.UserSimpleVO;
import com.hsx.manyue.modules.feed.service.IPostService;
import com.hsx.manyue.modules.oss.service.IOssService;
import com.hsx.manyue.modules.user.model.entity.UserEntity; // 假设用户实体路径
import com.hsx.manyue.modules.user.service.IUserService; // 假设用户服务路径
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, PostEntity> implements IPostService {
 
    private final PostImageMapper postImageMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;
    private final IUserService userService; // 注入用户服务
    private final WebSocketServer webSocketServer; // 注入WebSocket服务
    private final IOssService ossService; // 注入OSS服务


    @Override
    @Transactional
    public PostCommentVO addComment(Long postId, PostCommentParam param, Long userId) {
        // 1. 获取帖子和评论人信息
        PostEntity post = this.getById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        UserEntity fromUser = userService.getById(userId);
        if (fromUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 创建并保存评论实体
        PostCommentEntity comment = new PostCommentEntity();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(param.getContent());
        if (Objects.nonNull(param.getParentId()) && param.getParentId() > 0) {
            comment.setParentId(param.getParentId());
            comment.setReplyToUserId(param.getReplyToUserId());
        }
        postCommentMapper.insert(comment);

        // 3. 更新动态的评论数
        baseMapper.updateCommentCount(postId, 1);

        // 4. 发送通知
        // 使用一个Set来确保不给同一个人发送重复通知
        Set<Long> notifiedUserIds = new HashSet<>();

        // 通知被回复者
        if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(userId)) {
            webSocketServer.sendPostCommentNotification(comment.getReplyToUserId(), fromUser, post, comment.getContent());
            notifiedUserIds.add(comment.getReplyToUserId());
        }

        // 通知帖子作者（如果作者不是被回复者，且也不是评论者自己）
        if (!post.getUserId().equals(userId) && !notifiedUserIds.contains(post.getUserId())) {
            webSocketServer.sendPostCommentNotification(post.getUserId(), fromUser, post, comment.getContent());
        }

        // 5. 组装并返回VO
        return buildCommentVO(comment, fromUser, null);
    }

    @Override
    public Page<PostCommentVO> getComments(Long postId, Page<PostCommentEntity> page) {
        // 1. 分页查询根评论ID
        Page<Long> rootCommentIdPage = postCommentMapper.findRootCommentIds(page, postId);
        List<Long> rootIds = rootCommentIdPage.getRecords();

        if (CollectionUtils.isEmpty(rootIds)) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        // 2. 根据根评论ID，获取所有相关的评论（根评论+所有子孙评论）
        List<PostCommentEntity> allComments = postCommentMapper.findAllCommentsByRootIds(rootIds);

        // 3. 批量获取评论涉及到的所有用户信息
        Set<Long> userIds = allComments.stream()
                .map(PostCommentEntity::getUserId)
                .collect(Collectors.toSet());
        allComments.stream()
                .map(PostCommentEntity::getReplyToUserId)
                .filter(Objects::nonNull)
                .forEach(userIds::add);

        Map<Long, UserEntity> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        // 4. 将所有评论按 parentId 分组
        Map<Long, List<PostCommentEntity>> commentsGroupedByParent = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(PostCommentEntity::getParentId));

        // 5. 组装成树形结构的VO
        List<PostCommentVO> resultVos = rootIds.stream()
                .map(rootId -> allComments.stream().filter(c -> c.getId().equals(rootId)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(rootComment -> buildCommentTree(rootComment, commentsGroupedByParent, userMap))
                .collect(Collectors.toList());

        Page<PostCommentVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), rootCommentIdPage.getTotal());
        resultPage.setRecords(resultVos);
        return resultPage;
    }

    /**
     * 递归构建评论树
     */
    private PostCommentVO buildCommentTree(PostCommentEntity currentComment, Map<Long, List<PostCommentEntity>> groupedComments, Map<Long, UserEntity> userMap) {
        UserEntity user = userMap.get(currentComment.getUserId());
        UserEntity replyToUser = userMap.get(currentComment.getReplyToUserId());
        PostCommentVO vo = buildCommentVO(currentComment, user, replyToUser);

        List<PostCommentEntity> children = groupedComments.getOrDefault(currentComment.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<PostCommentVO> childVos = children.stream()
                    .map(child -> buildCommentTree(child, groupedComments, userMap))
                    .collect(Collectors.toList());
            vo.setReplies(childVos);
            vo.setReplyCount(childVos.size());
        } else {
            vo.setReplies(Collections.emptyList());
            vo.setReplyCount(0);
        }
        return vo;
    }

    private PostCommentVO buildCommentVO(PostCommentEntity comment, UserEntity user, UserEntity replyToUser) {
        PostCommentVO vo = new PostCommentVO();
        BeanUtils.copyProperties(comment, vo);

        if (user != null) {
            UserSimpleVO userVO = new UserSimpleVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }

        if (replyToUser != null) {
            UserSimpleVO replyToUserVO = new UserSimpleVO();
            BeanUtils.copyProperties(replyToUser, replyToUserVO);
            vo.setReplyToUser(replyToUserVO);
        }
        return vo;
    }


    @Override
    @Transactional
    public boolean createPost(PostCreateParam param, Long userId) {
        PostEntity post = new PostEntity();
        post.setUserId(userId);
        post.setContent(param.getContent());
        post.setLocation(param.getLocation());
        post.setStatus(1); // 默认公开
        System.out.println("userId"+post);
        this.save(post);
 
        if (!CollectionUtils.isEmpty(param.getImageUrls())) {
            for (int i = 0; i < param.getImageUrls().size(); i++) {
                PostImageEntity image = new PostImageEntity();
                image.setPostId(post.getId());
                image.setImageUrl(param.getImageUrls().get(i));
                image.setSortOrder(i);
                postImageMapper.insert(image);
            }
        }
        return true;
    }
 
    @Override
    public Page<PostVO> getPostFeed(Page<PostEntity> page, Long currentUserId) {
        // 1. 分页查询Post
        LambdaQueryWrapper<PostEntity> query = Wrappers.<PostEntity>lambdaQuery()
                .eq(PostEntity::getIsDelete, false)
                .eq(PostEntity::getStatus, 1)
                .orderByDesc(PostEntity::getCreateTime);
        Page<PostEntity> postEntityPage = this.page(page, query);
        List<PostEntity> postEntities = postEntityPage.getRecords();
 
        if (CollectionUtils.isEmpty(postEntities)) {
            return new Page<>();
        }
 
        // 2. 批量获取关联数据 (防N+1查询)
        List<Long> postIds = postEntities.stream().map(PostEntity::getId).collect(Collectors.toList());
        List<Long> authorIds = postEntities.stream().map(PostEntity::getUserId).collect(Collectors.toList());
 
        // 2.1 批量查作者信息
        Map<Long, UserEntity> userMap = userService.listByIds(authorIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        // 2.2 批量查图片
        Map<Long, List<PostImageEntity>> imageMap = postImageMapper.selectList(
                Wrappers.<PostImageEntity>lambdaQuery().in(PostImageEntity::getPostId, postIds)
                        .orderByAsc(PostImageEntity::getSortOrder)
        ).stream().collect(Collectors.groupingBy(PostImageEntity::getPostId));
 
        // 2.3 批量查当前用户点赞状态
        Map<Long, PostLikeEntity> likeMap = postLikeMapper.selectList(
                Wrappers.<PostLikeEntity>lambdaQuery()
                        .in(PostLikeEntity::getPostId, postIds)
                        .eq(PostLikeEntity::getUserId, currentUserId)
        ).stream().collect(Collectors.toMap(PostLikeEntity::getPostId, l -> l));
        
        // 3. 组装VO
        List<PostVO> postVOs = postEntities.stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);
            
            // 作者信息
            UserEntity author = userMap.get(post.getUserId());
            if (author != null) {
                UserSimpleVO authorVO = new UserSimpleVO();
                BeanUtils.copyProperties(author, authorVO);
                vo.setAuthor(authorVO);
            }
            
            // 图片列表
            List<PostImageEntity> images = imageMap.getOrDefault(post.getId(), Collections.emptyList());
            vo.setImageUrls(images.stream().map(PostImageEntity::getImageUrl).collect(Collectors.toList()));
            
            // 是否点赞
            vo.setIsLiked(likeMap.containsKey(post.getId()));
            
            return vo;
        }).collect(Collectors.toList());
        
        Page<PostVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(postVOs);
        return resultPage;
    }

    @Override
    @Transactional
    public boolean likePost(Long postId, Long userId) {
        // 检查帖子是否存在
        PostEntity post = this.getById(postId);
        if (post == null) {
            return false; // 或者抛出异常
        }

        // 防止重复点赞
        long count = postLikeMapper.selectCount(
                Wrappers.<PostLikeEntity>lambdaQuery().eq(PostLikeEntity::getPostId, postId).eq(PostLikeEntity::getUserId, userId)
        );
        if (count > 0) {
            return true; // 已点赞, 操作成功
        }

        // 插入点赞记录
        PostLikeEntity like = new PostLikeEntity();
        like.setPostId(postId);
        like.setUserId(userId);
        postLikeMapper.insert(like);

        // 更新点赞数
        baseMapper.updateLikeCount(postId, 1);

        // **========= 新增：发送点赞通知 =========**
        // 自己点赞自己的动态不发通知
        if (!post.getUserId().equals(userId)) {
            // 获取点赞者信息
            UserEntity fromUser = userService.getById(userId);
            if (fromUser != null) {
                // 生成内容摘要
                String contentSnippet = "";
                if (StringUtils.hasText(post.getContent())) {
                    contentSnippet = post.getContent().length() > 20
                            ? post.getContent().substring(0, 20) + "..."
                            : post.getContent();
                } else {
                    contentSnippet = "[图片动态]";
                }

                // 调用 WebSocket 服务发送通知
                webSocketServer.sendPostLikeNotification(
                        post.getUserId(),      // toUserId: 帖子作者
                        fromUser.getId(),      // fromUserId: 点赞者
                        fromUser.getNick(),    // fromUserNick: 点赞者昵称
                        post.getId(),          // postId: 帖子ID
                        contentSnippet         // postContentSnippet: 内容摘要
                );
            }
        }
        // **========= 新增逻辑结束 =========**

        return true;
    }
 
    @Override
    @Transactional
    public boolean unlikePost(Long postId, Long userId) {
        int deletedRows = postLikeMapper.delete(
            Wrappers.<PostLikeEntity>lambdaQuery().eq(PostLikeEntity::getPostId, postId).eq(PostLikeEntity::getUserId, userId)
        );
        
        if (deletedRows > 0) {
            baseMapper.updateLikeCount(postId, -1);
        }
        return true;
    }
 


    @Override
    public String uploadImage(MultipartFile file) {
        // 直接调用IOssService的实现
        return ossService.uploadFile(file);
    }

    private PostCommentVO buildCommentVO(PostCommentEntity comment) {
        PostCommentVO vo = new PostCommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        // 查评论人信息
        UserEntity user = userService.getById(comment.getUserId());
        if (user != null) {
            UserSimpleVO userVO = new UserSimpleVO();
            BeanUtils.copyProperties(user, userVO);
            vo.setUser(userVO);
        }
 
        // 如果是回复，查被回复人信息
        if (comment.getReplyToUserId() != null) {
            UserEntity replyToUser = userService.getById(comment.getReplyToUserId());
            if (replyToUser != null) {
                UserSimpleVO replyToUserVO = new UserSimpleVO();
                BeanUtils.copyProperties(replyToUser, replyToUserVO);
                vo.setReplyToUser(replyToUserVO);
            }
        }
        return vo;
    }
}