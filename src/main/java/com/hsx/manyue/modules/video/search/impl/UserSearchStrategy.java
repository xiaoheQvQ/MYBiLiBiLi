package com.hsx.manyue.modules.video.search.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsx.manyue.common.enums.VideoStatusEnum;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.user.service.IUserService;
import com.hsx.manyue.modules.video.mapper.VideoMapper;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import com.hsx.manyue.modules.video.search.VideoSearchStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户搜索策略：根据关键词在用户昵称中查找其发布的视频
 */
@Component
@RequiredArgsConstructor
public class UserSearchStrategy implements VideoSearchStrategy {

    private final IUserService userService;
    private final VideoMapper videoMapper;

    @Override
    public boolean supports(String type) {
        return "user".equals(type);
    }

    @Override
    public IPage<VideoDTO> search(VideoQueryParam param) {
        String keyword = param.getKeyword();
        if (StrUtil.isBlank(keyword)) {
            return new Page<>(param.getCurrent(), param.getSize());
        }

        // 1. 查找匹配的用户
        List<UserEntity> userList = userService.lambdaQuery()
                .like(UserEntity::getNick, keyword)
                .list();

        if (CollUtil.isEmpty(userList)) {
            return new Page<>(param.getCurrent(), param.getSize());
        }

        List<Long> userIds = userList.stream().map(UserEntity::getId).collect(Collectors.toList());

        // 2. 查询这些用户发布的已公开视频
        Page<VideoEntity> videoPage = videoMapper.selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.lambdaQuery(VideoEntity.class)
                        .in(VideoEntity::getUserId, userIds)
                        .eq(VideoEntity::getStatus, VideoStatusEnum.PUBLISHED)
                        .orderByDesc(VideoEntity::getCreateTime)
        );

        // 3. 转换结果并填充元数据
        return convertToDTO(videoPage);
    }

    private IPage<VideoDTO> convertToDTO(Page<VideoEntity> page) {
        Page<VideoDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(entity -> BeanUtil.toBean(entity, VideoDTO.class))
                .collect(Collectors.toList()));
        return result;
    }
}
