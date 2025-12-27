package com.hsx.manyue.modules.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.music.entity.MusicToplist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToplistMapper extends BaseMapper<MusicToplist> {
}
