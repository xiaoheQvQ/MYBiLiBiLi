package com.hsx.manyue.modules.Live.service.impl;


import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.Live.mapper.aLiveMapper;
import com.hsx.manyue.modules.Live.model.dto.aLiveDTO;
import com.hsx.manyue.modules.Live.model.entity.aLiveEntity;
import com.hsx.manyue.modules.Live.service.aLiveSerive;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.user.service.impl.UserSubscriptionServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class aLiveImpl extends ServiceImpl<aLiveMapper, aLiveEntity> implements aLiveSerive {

    private final WebSocketServer webSocketServer;

    private final UserSubscriptionServiceImpl subscriptionService;

    private final  String LiveServer = "rtmp://8.140.29.27:8080/live/" ;

    @Override
    public String generateStreamKey(Long userId) {
        LambdaQueryWrapper<aLiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(aLiveEntity::getUserId, userId)
                .eq(aLiveEntity::getIsLiving, 1);
        int count = (int) this.count(queryWrapper);

        if (count > 0) {
            // User already has an active live stream, return existing pushUrl
            aLiveEntity existing = this.getOne(queryWrapper);
            return existing.getPushUrl();
        }

        String streamKey = String.valueOf(userId);


        String pushUrl = LiveServer+streamKey;

        aLiveEntity aLiveEntity = new aLiveEntity();
        aLiveEntity.setUserId(userId);
        aLiveEntity.setIsLiving(1);
        aLiveEntity.setStreamKey(streamKey);
        aLiveEntity.setPushUrl(pushUrl);


        // 存入数据库
        this.save(aLiveEntity);

        return pushUrl;
    }

    @Override
    public void notifyFans(Long userId,String currentUserName) {
        List<Long> followIds = subscriptionService.getSubscriberIds(userId);


        System.out.println("userId:"+userId);
        String publishId = userId.toString();
        System.out.println("publishId:"+publishId);
        webSocketServer.notifyFans(followIds,publishId,currentUserName);

    }

    @Override
    public void unPublish(Long userId) {
        LambdaUpdateWrapper<aLiveEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(aLiveEntity::getUserId, userId)
                .eq(aLiveEntity::getIsLiving, 1)
                .set(aLiveEntity::getIsLiving, 0)
                .set( aLiveEntity::getIsDelete, true);

        this.update(updateWrapper);
    }

    @Override
    public List<aLiveDTO> getLivingStreams() {
            return baseMapper.selectLivingStreams();
    }
}
