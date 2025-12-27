package com.hsx.manyue.modules.video.search.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import com.hsx.manyue.common.enums.VideoStatusEnum;
import com.hsx.manyue.modules.video.mapper.VideoMapper;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import com.hsx.manyue.modules.video.search.VideoSearchStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 视频内容搜索策略：在标题和描述中模糊搜索
 */
@Component
@RequiredArgsConstructor
public class VideoContentSearchStrategy implements VideoSearchStrategy {

    private final VideoMapper videoMapper;

    @Override
    public boolean supports(String type) {
        // 默认为 content 搜索或者 null
        return StrUtil.isBlank(type) || "video".equals(type) || "content".equals(type);
    }

    @Override
    public IPage<VideoDTO> search(VideoQueryParam param) {
        String keyword = param.getKeyword();
        LambdaQueryWrapper<VideoEntity> wrapper = Wrappers.lambdaQuery(VideoEntity.class)
                .eq(VideoEntity::getStatus, VideoStatusEnum.PUBLISHED);

        if (StrUtil.isNotBlank(keyword)) {
            String[] keywords = keyword.split(" ");
            wrapper.and(w -> {
                for (String k : keywords) {
                    w.or().like(VideoEntity::getTitle, "%" + k + "%")
                            .or().like(VideoEntity::getDescription, "%" + k + "%");
                }
            });
        }

        VideoAreaEnum area = param.getArea();
        if (area != null) {
            wrapper.eq(VideoEntity::getArea, area);
        }

        // 模拟随机排序逻辑
        String randomOrderSql = param.getSeed() == 0 ? "RAND()" : "RAND(" + param.getSeed() + ")";
        wrapper.last("ORDER BY " + randomOrderSql);

        Page<VideoEntity> videoPage = videoMapper.selectPage(new Page<>(param.getCurrent(), param.getSize()), wrapper);

        Page<VideoDTO> result = new Page<>(videoPage.getCurrent(), videoPage.getSize(), videoPage.getTotal());
        result.setRecords(videoPage.getRecords().stream()
                .map(entity -> BeanUtil.toBean(entity, VideoDTO.class))
                .collect(Collectors.toList()));
        return result;
    }
}
