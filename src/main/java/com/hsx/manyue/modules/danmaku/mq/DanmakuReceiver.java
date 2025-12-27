package com.hsx.manyue.modules.danmaku.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.config.MQConfig;
import com.hsx.manyue.modules.danmaku.mapper.DanmakuMapper;
import com.hsx.manyue.modules.danmaku.model.dto.DplayerDanmakuDTO;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;
import com.hsx.manyue.modules.danmaku.websocket.DanmakuWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/**
 * 弹幕消息队列消费者
 */
@Component
@Slf4j
public class DanmakuReceiver {

    @RabbitListener(queues = MQConfig.DANAMKU_QUEUE)
    public void receive(String message) throws IOException {
        log.info("{} 消费者开始消费消息", MQConfig.DANAMKU_QUEUE);
        DplayerDanmakuDTO dto = JSONUtil.toBean(message, DplayerDanmakuDTO.class);
        
        DanmakuWebSocket.VideoSessionInfo videoSessionInfo = DanmakuWebSocket.getVideoSessions().get(dto.getPlayer());
        if (videoSessionInfo == null) {
            return;
        }
        Collection<Session> sessions = videoSessionInfo.getAllSession();

        for (Session target : sessions) {
            if (target.isOpen() && !StrUtil.equals(target.getId(), dto.getSessionId())) {
                target.getBasicRemote().sendText(message);
            }
        }


    }
}
