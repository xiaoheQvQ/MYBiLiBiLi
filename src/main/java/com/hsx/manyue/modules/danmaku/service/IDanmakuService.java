package com.hsx.manyue.modules.danmaku.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;

import java.util.List;

/**
 * 弹幕表 服务类
 */
public interface IDanmakuService extends IService<DanmakuEntity> {

    List<Object[]> parseToArrayInfo(List<String> range);

    List<Object[]> getDanmakusAsArrayInfoByVideoId(Long videoId);

    long countByVideoId(Long videoId);
}
