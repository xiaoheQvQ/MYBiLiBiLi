package com.hsx.manyue.modules.danmaku.websocket;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.hsx.manyue.common.config.MQConfig;
import com.hsx.manyue.modules.danmaku.mapper.DanmakuMapper;
import com.hsx.manyue.modules.danmaku.model.dto.DplayerDanmakuDTO;
import com.hsx.manyue.modules.danmaku.model.entity.DanmakuEntity;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.service.IVideoService;
import com.hsx.manyue.modules.video.service.impl.VideoServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ServerEndpoint("/danmaku/{videoId}")
@Component
@Slf4j
public class DanmakuWebSocket {

    private Session session;
    private String sessionId;



    @Getter
    private static final ConcurrentHashMap<Long, VideoSessionInfo> videoSessions = new ConcurrentHashMap<>();
    private static IVideoService videoService;
    private static RedisTemplate<String, String> redisTemplate;
    private static RabbitTemplate rabbitTemplate;
    private Long videoId;

    @Autowired
    public void setVideoService(IVideoService videoService) {
        DanmakuWebSocket.videoService = videoService;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        DanmakuWebSocket.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        DanmakuWebSocket.rabbitTemplate = rabbitTemplate;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("videoId") Long videoId) throws IOException {
        VideoEntity byId = videoService.getById(videoId);
        long viewers = putVideoSessionInfo(session, videoId);

        sendViewers(videoId);

        log.info("连接成功, 当前视频：{}，观看人数：{}", videoId, viewers);
    }

    private void sendViewers(Long videoId) throws IOException {
        DplayerDanmakuDTO danmaku = new DplayerDanmakuDTO();
        danmaku.setViewers(getViewers(videoId)).setPlayer(videoId);
        rabbitTemplate.convertAndSend(MQConfig.DANAMKU_EXCHANGE, "", JSON.toJSONString(danmaku));
    }

    private long putVideoSessionInfo(Session session, Long videoId) {
        this.session = session;
        this.sessionId = session.getId();
        this.videoId = videoId;
        VideoSessionInfo videoSessionInfo = videoSessions.getOrDefault(videoId, new VideoSessionInfo());
        videoSessionInfo.addSession(sessionId, session);
        videoSessions.put(videoId, videoSessionInfo);
        return videoSessionInfo.getViewers();
    }

    @OnClose
    public void onClose(@PathParam("videoId") Long videoId) throws IOException {
        VideoSessionInfo videoSessionInfo = videoSessions.get(videoId);
        videoSessionInfo.removeSession(sessionId);
        log.info("视频：{}，用户断开连接，观看人数：{}", videoId, videoSessionInfo.getViewers());
        if (!videoSessionInfo.hasViewers()) {
            videoSessions.remove(videoId);
            log.info("当前视频：{}，已无人观看", videoId);
        }
        sendViewers(videoId);
    }

    /**
     * 接受到消息后，转发到各个会话
     * 第一次压测：平均吞吐量 ：238/s
     *
     * @param videoId
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(@PathParam("videoId") Long videoId, String message) throws IOException {
        DplayerDanmakuDTO danmaku = JSONUtil.toBean(message, DplayerDanmakuDTO.class);
        danmaku.setSessionId(this.sessionId);
        danmaku.setViewers(getViewers(videoId));

        log.info("视频：{} 接受到弹幕：{}", videoId, message);


        // 转发给消息队列
        rabbitTemplate.convertAndSend(MQConfig.DANAMKU_EXCHANGE, "", JSON.toJSONString(danmaku));

        DplayerDanmakuDTO dto = JSONUtil.toBean(message, DplayerDanmakuDTO.class);
        // 存储到数据库
        DanmakuEntity danmakuEntity = new DanmakuEntity();
        danmakuEntity.setVideoId(dto.getPlayer()); // 假设 player 是视频ID
        danmakuEntity.setContent(dto.getText());    // 弹幕内容
        danmakuEntity.setColor(dto.getColor());     // 弹幕颜色
        danmakuEntity.setNick(dto.getAuthor());    //弹幕发布者的昵称
        danmakuEntity.setTime(dto.getTime());      // 发送时间
        danmakuEntity.setUserId(dto.getUserId());   // 发送用户ID（如果有）
        danmakuEntity.setColor(dto.getColor());
        danmakuEntity.setPosition(dto.getType());
        videoService.insertDanMaKu(danmakuEntity);


        // 缓存到 Redis
        redisTemplate.opsForList().rightPush("danmaku:" + videoId, JSONUtil.toJsonStr(danmaku.toArrayInfo()));
        redisTemplate.opsForList().rightPush("danmaku:new:" + videoId, message);

        System.out.println("弹幕保存实现");

    }

    @OnError
    public void onError(@PathParam("videoId") Long videoId, Session session, Throwable error) {
        log.error("视频 {} 会话发生错误", videoId, error);
        if (videoSessions.containsKey(videoId)) {
            VideoSessionInfo videoSessionInfo = videoSessions.get(videoId);
            videoSessionInfo.removeSession(session.getId());
        }
    }

    public long getViewers(Long videoId) {
        VideoSessionInfo videoSessionInfo = videoSessions.get(videoId);
        if (videoSessionInfo != null) {
            return videoSessionInfo.getViewers();
        }
        return 0;
    }

    public static class VideoSessionInfo {

        private final AtomicLong viewer = new AtomicLong(0);
        private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

        public long getViewers() {
            return viewer.get();
        }

        public void addSession(String sessionId, Session session) {
            if (!sessions.containsKey(sessionId)) {
                sessions.put(sessionId, session);
                viewer.incrementAndGet();
            }
        }

        public void removeSession(String sessionId) {
            if (sessions.containsKey(sessionId)) {
                sessions.remove(sessionId);
                viewer.decrementAndGet();
            }
        }

        public Session getSession(String sessionId) {
            return sessions.get(sessionId);
        }

        public Collection<Session> getAllSession() {
            return sessions.values();
        }

        public boolean hasViewers() {
            return !sessions.isEmpty();
        }
    }
}
