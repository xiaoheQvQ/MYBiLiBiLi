package com.hsx.manyue.modules.Live.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hsx.manyue.modules.Live.model.dto.aLiveDTO;
import com.hsx.manyue.modules.Live.model.entity.aLiveEntity;
import com.hsx.manyue.modules.video.model.entity.TagEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface aLiveMapper extends BaseMapper<aLiveEntity> {


    @Select("SELECT ls.*, u.nick as user_nickname, u.avatar as user_avatar " +
            "FROM t_live_stream ls " +
            "LEFT JOIN t_user u ON ls.user_id = u.id where ls.is_delete = 0")
    List<aLiveDTO> selectLivingStreams();

}
