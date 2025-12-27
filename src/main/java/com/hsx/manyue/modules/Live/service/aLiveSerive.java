package com.hsx.manyue.modules.Live.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.Live.model.dto.aLiveDTO;
import com.hsx.manyue.modules.Live.model.entity.aLiveEntity;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;

import java.util.List;


public interface aLiveSerive extends IService<aLiveEntity> {
    String generateStreamKey(Long userId);

    void notifyFans(Long userId,String currentUserName);

    void unPublish(Long userId);

    List<aLiveDTO> getLivingStreams();
}
