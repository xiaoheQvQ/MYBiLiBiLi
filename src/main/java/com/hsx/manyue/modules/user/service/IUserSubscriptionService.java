package com.hsx.manyue.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.model.entity.UserSubscriptionEntity;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;

import java.util.List;

/**
 * 用户订阅表
 */
public interface IUserSubscriptionService extends IService<UserSubscriptionEntity> {

    void subscribe(Long sourceUserId, Long authorUserId);

    void cancelSubscribe(Long sourceUserId, Long authorUserId);

    void publishVideo(Long videoId);

    void notifyUsersOfVideos();

    List<VideoDTO> getMsg(Long userId);

    List<ChatMessage> getChatMsg(Long userId);

    void consumeMsg(Long userId, Integer index);

    /**
     * 批量删除用户的所有通知
     * @param userId 用户ID
     */
    void consumeAllMsg(Long userId);

    Boolean isSubscription(Long currentUserId, Long userId);

    List<UserDTO> getSubscriptions(Long userId);

    List<UserDTO> getSubscribedList(Long userId);

    /**
     * 统计用户的粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    Long countFollowers(Long userId);

    /**
     * 统计用户关注的人数
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    Long countFollowing(Long userId);

    /**
     * 获取指定用户的所有订阅者ID列表
     *
     * @param userId 被订阅的用户ID
     * @return 订阅者ID列表
     */
    List<Long> getSubscriberIds(Long userId);

    void consumeChatMsg(Long userId, Integer index);

    void consumeAllChatMsg(Long userId);
}
