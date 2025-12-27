package com.hsx.manyue.modules.anime.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.anime.mapper.AnimeCommentMapper;
import com.hsx.manyue.modules.anime.mapper.AnimeRatingMapper;
import com.hsx.manyue.modules.anime.model.dto.AnimeCommentDTO;
import com.hsx.manyue.modules.anime.model.dto.AnimeRatingDTO;
import com.hsx.manyue.modules.anime.model.entity.AnimeCommentEntity;
import com.hsx.manyue.modules.anime.model.entity.AnimeRatingEntity;
import com.hsx.manyue.modules.anime.model.service.AnimeCommentService;
import com.hsx.manyue.modules.user.model.entity.UserEntity;

import com.hsx.manyue.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimeCommentServiceImpl extends ServiceImpl<AnimeCommentMapper, AnimeCommentEntity> implements AnimeCommentService {
    private final AnimeRatingMapper animeRatingMapper;
    private final IUserService userService;

    @Override
    @Transactional
    public AnimeCommentDTO addComment(AnimeCommentDTO commentDTO) {
        AnimeCommentEntity commentEntity = new AnimeCommentEntity();
        BeanUtils.copyProperties(commentDTO, commentEntity);
        
        // 保存评论
        save(commentEntity);
        
        // 如果是回复评论，更新父评论的回复数
        if (commentDTO.getParentId() != null) {
            AnimeCommentEntity parentComment = getById(commentDTO.getParentId());
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            updateById(parentComment);
        }
        
        // 返回DTO
        AnimeCommentDTO result = new AnimeCommentDTO();
        BeanUtils.copyProperties(commentEntity, result);
        
        // 设置用户信息
        UserEntity user = userService.getById(commentDTO.getUserId());
        if (user != null) {
            result.setUsername(user.getNick());
            result.setAvatar(user.getAvatar());
        }
        
        return result;
    }

    @Override
    public List<AnimeCommentDTO> listComments(Long seriesId, Long episodeId, Integer page, Integer size) {
        // 查询一级评论
        QueryWrapper<AnimeCommentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("series_id", seriesId)
                .eq(episodeId != null, "episode_id", episodeId)
                .isNull("parent_id")
                .orderByDesc("create_time");
        
        Page<AnimeCommentEntity> pageResult = page(new Page<>(page, size), queryWrapper);
        
        List<AnimeCommentEntity> commentEntities = pageResult.getRecords();
        if (commentEntities.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取所有评论ID
        List<Long> commentIds = commentEntities.stream()
                .map(AnimeCommentEntity::getId)
                .collect(Collectors.toList());
        
        // 查询回复评论
        QueryWrapper<AnimeCommentEntity> replyWrapper = new QueryWrapper<>();
        replyWrapper.in("root_id", commentIds)
                .orderByAsc("create_time");
        List<AnimeCommentEntity> replyEntities = list(replyWrapper);
        
        // 获取所有用户ID
        Set<Long> userIds = new HashSet<>();
        commentEntities.forEach(c -> userIds.add(c.getUserId()));
        replyEntities.forEach(r -> userIds.add(r.getUserId()));
        
        // 批量查询用户信息
        Map<Long, UserEntity> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        // 构建评论DTO列表
        List<AnimeCommentDTO> commentDTOs = commentEntities.stream().map(c -> {
            AnimeCommentDTO dto = new AnimeCommentDTO();
            BeanUtils.copyProperties(c, dto);
            
            UserEntity user = userMap.get(c.getUserId());
            if (user != null) {
                dto.setUsername(user.getNick());
                dto.setAvatar(user.getAvatar());
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 构建回复评论DTO列表
        Map<Long, List<AnimeCommentDTO>> replyMap = replyEntities.stream().map(r -> {
            AnimeCommentDTO dto = new AnimeCommentDTO();
            BeanUtils.copyProperties(r, dto);
            
            UserEntity user = userMap.get(r.getUserId());
            if (user != null) {
                dto.setUsername(user.getNick());
                dto.setAvatar(user.getAvatar());
            }
            
            return dto;
        }).collect(Collectors.groupingBy(AnimeCommentDTO::getRootId));
        
        // 将回复评论设置到一级评论中
        commentDTOs.forEach(c -> {
            List<AnimeCommentDTO> replies = replyMap.getOrDefault(c.getId(), Collections.emptyList());
            c.setReplies(replies);
        });
        
        return commentDTOs;
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        // 这里需要实现点赞逻辑，可以单独建一个点赞表
        // 简化实现，直接更新评论点赞数
        AnimeCommentEntity comment = getById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            updateById(comment);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean unlikeComment(Long commentId, Long userId) {
        // 这里需要实现取消点赞逻辑
        // 简化实现，直接更新评论点赞数
        AnimeCommentEntity comment = getById(commentId);
        if (comment != null && comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            updateById(comment);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public AnimeRatingDTO addRating(AnimeRatingDTO ratingDTO) {
        // 查询是否已评分
        AnimeRatingEntity existingRating = animeRatingMapper.selectOne(new QueryWrapper<AnimeRatingEntity>()
                .eq("series_id", ratingDTO.getSeriesId())
                .eq("user_id", ratingDTO.getUserId()));
        
        AnimeRatingEntity ratingEntity = new AnimeRatingEntity();
        ratingEntity.setSeriesId(ratingDTO.getSeriesId());
        ratingEntity.setUserId(ratingDTO.getUserId());
        ratingEntity.setScore(ratingDTO.getScore());
        
        if (existingRating != null) {
            // 更新评分
            ratingEntity.setId(existingRating.getId());
            animeRatingMapper.updateById(ratingEntity);
        } else {
            // 新增评分
            animeRatingMapper.insert(ratingEntity);
        }
        
        // 返回最新的评分信息
        return getRatingInfo(ratingDTO.getSeriesId());
    }

    @Override
    public AnimeRatingDTO getRatingInfo(Long seriesId) {
        // 查询评分统计信息
        Map<String, Object> ratingStats = animeRatingMapper.selectRatingStats(seriesId);
        
        AnimeRatingDTO ratingDTO = new AnimeRatingDTO();
        ratingDTO.setSeriesId(seriesId);
        
        if (ratingStats != null && !ratingStats.isEmpty()) {
            BigDecimal averageScore = (BigDecimal) ratingStats.get("averageScore");
            Long ratingCount = (Long) ratingStats.get("ratingCount");
            
            ratingDTO.setAverageScore(averageScore != null ? 
                averageScore.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            ratingDTO.setRatingCount(ratingCount != null ? ratingCount.intValue() : 0);
        } else {
            ratingDTO.setAverageScore(BigDecimal.ZERO);
            ratingDTO.setRatingCount(0);
        }
        
        return ratingDTO;
    }

    @Override
    public AnimeRatingDTO getUserRating(Long seriesId, Long userId) {
        AnimeRatingEntity ratingEntity = animeRatingMapper.selectOne(new QueryWrapper<AnimeRatingEntity>()
                .eq("series_id", seriesId)
                .eq("user_id", userId));
        
        AnimeRatingDTO ratingDTO = new AnimeRatingDTO();
        ratingDTO.setSeriesId(seriesId);
        ratingDTO.setUserId(userId);
        
        if (ratingEntity != null) {
            ratingDTO.setScore(ratingEntity.getScore());
        }
        
        // 设置平均分和评分人数
        AnimeRatingDTO ratingInfo = getRatingInfo(seriesId);
        ratingDTO.setAverageScore(ratingInfo.getAverageScore());
        ratingDTO.setRatingCount(ratingInfo.getRatingCount());
        
        return ratingDTO;
    }
}