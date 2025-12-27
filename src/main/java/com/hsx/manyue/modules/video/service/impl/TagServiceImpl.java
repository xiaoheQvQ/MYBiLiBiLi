package com.hsx.manyue.modules.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.video.mapper.TagMapper;
import com.hsx.manyue.modules.video.model.dto.TagDTO;
import com.hsx.manyue.modules.video.model.entity.TagEntity;
import com.hsx.manyue.modules.video.service.ITagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频标签表 服务实现类
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagEntity> implements ITagService {

    @Override
    public List<TagDTO> getTags() {
        return baseMapper.getTags();
    }
}
