package com.hsx.manyue.modules.user.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.utils.RedisUtil;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.chat.model.dto.NewChatMessageDto;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.user.mapper.UserSubscriptionMapper;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.model.entity.UserSubscriptionEntity;
import com.hsx.manyue.modules.user.service.IUserSubscriptionService;
import com.hsx.manyue.modules.video.model.dto.NewMessageDto;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.service.IVideoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户订阅表 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl extends ServiceImpl<UserSubscriptionMapper, UserSubscriptionEntity> implements IUserSubscriptionService {
    @Lazy
    @Resource
    private  IVideoService videoService;
    private final WebSocketServer webSocketServer;
    private final LinkedBlockingQueue<Long> publishVideoIds = new LinkedBlockingQueue<>(30);
    private final ThreadPoolExecutor notifyUsersForVideosExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), ThreadUtil.createThreadFactory("subscribe-video-thread-"));
    @Resource
    private RedisUtil redisUtil;

    @Override
    public void subscribe(Long sourceUserId, Long authorUserId) {
        UserSubscriptionEntity entity = this.lambdaQuery().eq(UserSubscriptionEntity::getUserId, sourceUserId)
                .eq(UserSubscriptionEntity::getAuthorId, authorUserId).one();
        Assert.isNull(entity, "已经关注该用户");
        entity = new UserSubscriptionEntity().setUserId(sourceUserId).setAuthorId(authorUserId);
        this.save(entity);
    }

    @Override
    public void cancelSubscribe(Long sourceUserId, Long authorUserId) {
        UserSubscriptionEntity entity = this.lambdaQuery().eq(UserSubscriptionEntity::getUserId, sourceUserId)
                .eq(UserSubscriptionEntity::getAuthorId, authorUserId).one();
        Assert.notNull(entity, "未关注该用户");
        this.remove(this.lambdaQuery().eq(UserSubscriptionEntity::getUserId, sourceUserId)
                .eq(UserSubscriptionEntity::getAuthorId, authorUserId)
                .getWrapper());
    }

    @Override
    public void publishVideo(Long videoId) {
        try {
            publishVideoIds.put(videoId);
        } catch (InterruptedException e) {
            log.error("发布新的视频到队列中，遇到中断，videoId：{}", videoId);
        }
    }

    @Override
    @PostConstruct
    public void notifyUsersOfVideos() {
        notifyUsersForVideosExecutor.execute(() -> {
            try {
                while (true) {
                    Long videoId = publishVideoIds.take();
                    log.info("订阅功能开始工作");
                    VideoDTO dto = videoService.getVideoNotPlayUrl(videoId);
                    List<Long> userIds = this.lambdaQuery().eq(UserSubscriptionEntity::getAuthorId, dto.getUserId())
                            .list().stream().map(UserSubscriptionEntity::getUserId)
                            .collect(Collectors.toList());
                    String msg = JSONUtil.toJsonStr(dto);
                    for (Long userId : userIds) {
                        String key = RedisKeys.SUBSCRIBE + userId;
                        redisUtil.lSet(key, msg);
                        redisUtil.expire(key, 7, TimeUnit.DAYS);
                    }
                }
            } catch (InterruptedException e) {
                log.error("从队列中获取视频ID时发生错误");
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 离线期收到的视频动态
     */
    @Override
    public List<VideoDTO> getMsg(Long userId) {
        String key = RedisKeys.VIDEO_NOTIFICATIONS + userId;
        List<Object> msgJson = redisUtil.lGet(key, 0, -1);
        return msgJson.stream()
                .map(i -> JSONUtil.toBean(i.toString(), NewMessageDto.class).getData())
                .collect(Collectors.toList());
    }

    /**
     * 离线期收到的私信
     *
     * @return
     */
    @Override
    public List<ChatMessage> getChatMsg(Long userId) {
        String key = RedisKeys.PRIVATE_MESSAGES + userId;
        List<Object> messages = redisUtil.lGet(key, 0, -1);

        return messages.stream()
                .map(i -> JSONUtil.toBean(i.toString(), NewChatMessageDto.class).getData())
                .collect(Collectors.toList());
    }

    @Override
    public void consumeMsg(Long userId, Integer index) {
        String key = RedisKeys.VIDEO_NOTIFICATIONS + userId;
        Object value = redisUtil.lGetIndex(key, index);
        if (value == null) {
            return;
        }
        redisUtil.lRemove(key, 1, value);
    }

    @Override
    public void consumeAllMsg(Long userId) {
        String key = RedisKeys.VIDEO_NOTIFICATIONS + userId;
        redisUtil.del(key);
    }

    @Override
    public Boolean isSubscription(Long currentUserId, Long userId) {
        return this.lambdaQuery().eq(UserSubscriptionEntity::getUserId, currentUserId)
                .eq(UserSubscriptionEntity::getAuthorId, userId)
                .count() > 0;
    }

    @Override
    public List<UserDTO> getSubscriptions(Long userId) {
        return baseMapper.getSubscriptions(userId);
    }

    @Override
    public List<UserDTO> getSubscribedList(Long userId) {
        return baseMapper.getSubscribedList(userId);
    }

    @Override
    public Long countFollowers(Long userId) {
        // 统计有多少用户关注了当前用户 (当前用户是被关注者)
        return this.lambdaQuery()
                .eq(UserSubscriptionEntity::getAuthorId, userId)
                .count();
    }

    @Override
    public Long countFollowing(Long userId) {
        // 统计当前用户关注了多少其他用户 (当前用户是关注者)
        return this.lambdaQuery()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .count();
    }

    @Override
    public List<Long> getSubscriberIds(Long userId) {
        return this.lambdaQuery()
                .select(UserSubscriptionEntity::getUserId)
                .eq(UserSubscriptionEntity::getAuthorId, userId)
                .list()
                .stream()
                .map(UserSubscriptionEntity::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    public void consumeChatMsg(Long userId, Integer index) {
        String key = RedisKeys.PRIVATE_MESSAGES + userId;
        Object value = redisUtil.lGetIndex(key, index);
        if (value == null) {
            return;
        }
        redisUtil.lRemove(key, 1, value);
    }

    @Override
    public void consumeAllChatMsg(Long userId) {
        String key = RedisKeys.PRIVATE_MESSAGES + userId;
        redisUtil.del(key);
    }

    public void ChatSend(JSONObject message) {

            webSocketServer.handlePrivateMessage(message);

    }

    public void VideoChatSend(JSONObject message) {
        webSocketServer.handlePrivateVideoChatMessage(message);
    }

    public void VideoChatResponseSend(JSONObject message) {
        webSocketServer.handlePrivateVideoChatResponseMessage(message);
    }

    public void sendPrivateVideoChatEndMessage(JSONObject message) {
        webSocketServer.handlePrivateVideoChatEndMessage(message);
    }
}
