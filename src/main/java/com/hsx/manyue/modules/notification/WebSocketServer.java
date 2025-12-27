package com.hsx.manyue.modules.notification;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.enums.VideoStatusEnum;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.RedisUtil;

import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.chat.service.impl.ChatMessageServiceImpl;
import com.hsx.manyue.modules.feed.model.entity.PostEntity;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.video.model.entity.CommentEntity;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/notification")
@Component
@Slf4j
public class WebSocketServer {

    /**
     * 用于存储用户ID和WebSocket会话的映射
     * key: userId
     * value: session
     */
    private static final Map<Long, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    private static final Map<String, Set<Long>> LIVE_ROOM_USERS = new ConcurrentHashMap<>();

    private static ChatMessageServiceImpl chatMessageService;

    private static RedisUtil redisUtil;

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private static final Map<String, String> userToSessionMap = new ConcurrentHashMap<>();

    private static final Map<String, String> sessionToUserMap = new ConcurrentHashMap<>();

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        WebSocketServer.redisUtil = redisUtil;
    }

    @Autowired
    public void setChatMessageService(ChatMessageServiceImpl chatMessageService) {
        WebSocketServer.chatMessageService = chatMessageService;
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("新的WebSocket连接建立: {}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        // 从映射中移除会话
        USER_SESSIONS.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        log.info("WebSocket连接关闭: {}", session.getId());
    }


    @OnMessage
    public void onMessage(String message, Session session) {

        try {
            JSONObject jsonMessage = JSONUtil.parseObj(message);
            String type = jsonMessage.getStr("type");

            if ("auth".equals(type)) {
                handleAuth(jsonMessage.getStr("token"), session);  // 认证并绑定用户ID
            } else if ("PRIVATE_MESSAGE".equals(type)) {
                handlePrivateMessage(jsonMessage);
            }  else if ("JOIN_LIVE_ROOM".equals(type)) {
                handleJoinLiveRoom(jsonMessage, session);
            } else if ("LEAVE_LIVE_ROOM".equals(type)) {
                handleLeaveLiveRoom(jsonMessage);
            } else if ("LIVE_CHAT_MESSAGE".equals(type)) {
                handleLiveChatMessage(jsonMessage);
            } else if ("LIVE_GIFT_MESSAGE".equals(type)) {
                handleLiveGiftMessage(jsonMessage);
            }else if ("register".equals(type)) {
                handleRegister(jsonMessage, session);
            }else if ("offer".equals(type)) {
                handleOffer(jsonMessage, session);
            }else if ("answer".equals(type)) {
                handleAnswer(jsonMessage, session);
            }else if ("candidate".equals(type)) {
                handleCandidate(jsonMessage, session);
            }else if ("call".equals(type)) {
                handleCall(jsonMessage, session);
            }else if ("endCall".equals(type)) {
                handleEndCall(jsonMessage, session);
            } else if ("VIDEO_CALL_REQUEST".equals(type)) {
                handleVideoCallRequest(jsonMessage);
            } else if ("VIDEO_CALL_RESPONSE".equals(type)) {
                handleVideoCallResponse(jsonMessage);
            } else if ("VIDEO_CALL_ENDED".equals(type)) {
                handleVideoCallEnded(jsonMessage);
            } else if ( "COMMENT_REPLY".equals(type)){
                handleCommentReply(jsonMessage);

            } else if ("POST_LIKE".equals(type)) { // 新增分支，虽然此类型由后端触发，但保留以备将来扩展
                // 通常由后端直接调用 send 方法，客户端不会主动发此类型消息
                log.info("Received POST_LIKE message, but it should be server-initiated: {}", jsonMessage);
            }
            else if ( "COMMENT_REPLY".equals(type)){
                handleCommentReply(jsonMessage);
            }





        } catch (Exception e) {
            log.error("处理消息时发生错误", e);
        }
    }

    /**
     * 发送动态评论/回复通知
     *
     * @param toUserId           接收通知的用户ID (帖子作者或被回复者)
     * @param fromUser           评论/回复的用户实体
     * @param post               被评论/回复的帖子实体
     * @param commentContent     评论的内容
     */
    public void sendPostCommentNotification(Long toUserId, UserEntity fromUser, PostEntity post, String commentContent) {
        // 创建通知体
        JSONObject message = new JSONObject();
        message.set("type", "POST_COMMENT"); // 使用新类型

        JSONObject data = new JSONObject();
        data.set("id", cn.hutool.core.util.IdUtil.getSnowflakeNextId()); // 唯一ID
        data.set("fromUserId", fromUser.getId().toString());
        data.set("fromUserNick", fromUser.getNick());
        data.set("fromUserAvatar", fromUser.getAvatar());
        data.set("postId", post.getId().toString());
        data.set("commentContent", commentContent);

        // 生成帖子内容摘要
        String postContentSnippet = "";
        if (cn.hutool.core.util.StrUtil.isNotBlank(post.getContent())) {
            postContentSnippet = cn.hutool.core.util.StrUtil.sub(post.getContent(), 0, 20) + "...";
        } else {
            postContentSnippet = "[图片动态]";
        }
        data.set("postContentSnippet", postContentSnippet);
        data.set("createTime", new Date());

        message.set("data", data);
        String messageText = message.toString();

        // 检查用户是否在线
        Session session = USER_SESSIONS.get(toUserId);
        if (session != null && session.isOpen()) {
            // 在线，直接通过 WebSocket 发送
            try {
                session.getBasicRemote().sendText(messageText);
                log.info("成功发送动态评论通知 (在线) 给用户 {}: {}", toUserId, messageText);
            } catch (IOException e) {
                log.error("发送动态评论通知 (在线) 给用户 {} 失败", toUserId, e);
            }
        } else {
            // 不在线，存入 Redis
            String key = RedisKeys.POST_COMMENT_NOTIFICATIONS + toUserId;
            redisUtil.lSet(key, messageText);
            log.info("用户 {} 不在线，评论通知已存入Redis. Key: {}, Message: {}", toUserId, key, messageText);
        }
    }

    /**
     * 发送动态点赞通知
     * @param toUserId          接收通知的用户ID (帖子作者)
     * @param fromUserId        点赞的用户ID
     * @param fromUserNick      点赞用户的昵称
     * @param postId            被点赞的帖子ID
     * @param postContentSnippet 帖子内容的片段 (用于预览)
     */
    public void sendPostLikeNotification(Long toUserId, Long fromUserId, String fromUserNick, Long postId, String postContentSnippet) {
        // 构造通知消息体
        JSONObject message = new JSONObject();
        message.set("type", "POST_LIKE");

        JSONObject data = new JSONObject();
        // 使用雪花算法或者UUID生成唯一ID，便于前端管理
        data.set("id", cn.hutool.core.util.IdUtil.getSnowflakeNextId());
        data.set("fromUserId", fromUserId.toString());
        data.set("fromUserNick", fromUserNick);
        data.set("postId", postId.toString());
        data.set("postContentSnippet", postContentSnippet);
        data.set("createTime", new Date());

        message.set("data", data);
        String messageText = message.toString();

        // 检查接收用户是否在线
        Session session = USER_SESSIONS.get(toUserId);
        if (session != null && session.isOpen()) {
            // 用户在线，直接通过 WebSocket 发送
            try {
                session.getBasicRemote().sendText(messageText);
                log.info("成功发送点赞通知 (在线) 给用户 {}: {}", toUserId, messageText);
            } catch (IOException e) {
                log.error("发送点赞通知 (在线) 给用户 {} 失败", toUserId, e);
            }
        } else {
            // 用户不在线，存入 Redis 离线消息列表
            String key = RedisKeys.POST_LIKE_NOTIFICATIONS + toUserId;
            redisUtil.lSet(key, messageText);
            log.info("用户 {} 不在线，点赞通知已存入Redis. Key: {}, Message: {}", toUserId, key, messageText);
        }
    }


    /**
     * 发送新评论通知
     */
    public void sendComment(CommentEntity comment) {
        // 构造评论消息
        JSONObject commentMessage = new JSONObject();
        commentMessage.set("type", "NEW_COMMENT");

        JSONObject data = new JSONObject();
        data.set("id", comment.getId());
        data.set("userId", comment.getUserId().toString());
        data.set("videoId", comment.getVideoId().toString());
        data.set("content", comment.getContent());
        data.set("toUserId", comment.getToUserId().toString());
        data.set("parentId", comment.getParentId());
        data.set("createTime", comment.getCreateTime());
        data.set("nick", comment.getNick());
        data.set("videoPublishUserId", comment.getVideoPublishUserId());
        data.set("videoTitle", comment.getVideoTitle());
        data.set("videoId", comment.getVideoId().toString());


        commentMessage.set("data", data);
        System.out.println("测试111111111111111111112222222222");
        System.out.println(commentMessage);
        // 如果是回复评论(parentId != 0)，则发送给被回复的用户
        if (comment.getParentId() != null && comment.getParentId() != 0L) {
            Session replyToSession = USER_SESSIONS.get(comment.getToUserId());
            System.out.println("replyToSession"+replyToSession);
            if (replyToSession != null && replyToSession.isOpen()) {
                try {
                    replyToSession.getBasicRemote().sendText(commentMessage.toString());
                    log.info("发送评论回复通知给用户: {}", comment.getToUserId());

                } catch (IOException e) {
                    log.error("发送评论回复通知失败", e);
                }
            }
        }

    }

    public void sendCommentLevel(CommentEntity comment) {

        // todo 发送给视频发布者
        Session videoPublishSession = USER_SESSIONS.get(Long.valueOf(comment.getVideoPublishUserId()));
        System.out.println("发送给视频发布者："+comment);
       // System.out.println(videoPublishSession + ":::" +videoPublishSession.isOpen());
        if (comment == null || comment.getVideoPublishUserId() == null) {
            log.warn("评论或视频发布者ID为空");
            return;
        }
        // 构造评论消息
        JSONObject commentMessage = new JSONObject();
        commentMessage.set("type", "NEW_COMMENT");

        JSONObject data = new JSONObject();


        data.set("content", comment.getContent());
        data.set("nick", comment.getNick());
        data.set("videoPublishUserId", comment.getVideoPublishUserId());
        data.set("videoTitle", comment.getVideoTitle());
        data.set("videoId", comment.getVideoId().toString());


        commentMessage.set("data", data);


        if (videoPublishSession != null && videoPublishSession.isOpen()) {
            try {
                videoPublishSession.getBasicRemote().sendText(commentMessage.toString());
                log.info("发送评论回复通知给视频发布者: {}", comment.getVideoPublishUserId());

            } catch (IOException e) {
                log.error("发送评论回复通知失败", e);
            }
        }

    }


    /**
     * 处理评论回复
     */
    private void handleCommentReply(JSONObject jsonMessage) {

        System.out.println("测试111111111111111111113333333333333333");
        Long fromUserId = jsonMessage.getLong("fromUserId");
        Long toUserId = jsonMessage.getLong("toUserId");
        Long videoId = jsonMessage.getLong("videoId");
        Long parentId = jsonMessage.getLong("parentId");
        System.out.println("fromUserId"+fromUserId+"toUserId"+toUserId+"videoId"+videoId+"parentId"+parentId);
        String content = jsonMessage.getStr("content");

        // 创建评论实体
        CommentEntity comment = new CommentEntity();
        comment.setUserId(fromUserId);
        comment.setVideoId(videoId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setIsDelete(0);

        // 发送评论通知
        sendComment(comment);
    }

    // 在 WebSocketServer 类中添加以下方法
    private void handleVideoCallRequest(JSONObject jsonMessage) {
        Long fromUserId = jsonMessage.getLong("from");
        Long toUserId = jsonMessage.getLong("to");

        Session receiverSession = USER_SESSIONS.get(toUserId);
        if (receiverSession != null && receiverSession.isOpen()) {
            JSONObject callRequest = new JSONObject();
            callRequest.set("type", "VIDEO_CALL_REQUEST");
            callRequest.set("from", fromUserId.toString());
            callRequest.set("fromUserNick", jsonMessage.getStr("fromUserNick"));

            try {
                receiverSession.getBasicRemote().sendText(callRequest.toString());
                log.info("视频通话请求已发送给用户: {}", toUserId);
            } catch (IOException e) {
                log.error("发送视频通话请求失败", e);
            }
        } else {
            log.warn("接收方不在线，无法发送视频通话请求");
        }
    }



    private void handleVideoCallResponse(JSONObject jsonMessage) {
        Long fromUserId = jsonMessage.getLong("from");
        Long toUserId = jsonMessage.getLong("to");
        boolean accepted = jsonMessage.getBool("accepted");

        Session senderSession = USER_SESSIONS.get(toUserId); // toUserId是发起方
        if (senderSession != null && senderSession.isOpen()) {
            JSONObject response = new JSONObject();
            response.set("type", "VIDEO_CALL_RESPONSE");
            response.set("from", fromUserId.toString());
            response.set("accepted", accepted);

            try {
                senderSession.getBasicRemote().sendText(response.toString());
                log.info("视频通话响应已发送给用户: {}", toUserId);
            } catch (IOException e) {
                log.error("发送视频通话响应失败", e);
            }
        } else {
            log.warn("发起方不在线，无法发送视频通话响应");
        }
    }
    private void handleVideoCallEnded(JSONObject jsonMessage) {
        Long fromUserId = jsonMessage.getLong("from");
        Long toUserId = jsonMessage.getLong("to");

        Session receiverSession = USER_SESSIONS.get(toUserId);
        if (receiverSession != null && receiverSession.isOpen()) {
            JSONObject callEnded = new JSONObject();
            callEnded.set("type", "VIDEO_CALL_ENDED");
            callEnded.set("from", fromUserId.toString());

            try {
                receiverSession.getBasicRemote().sendText(callEnded.toString());
                log.info("视频通话结束通知已发送给用户: {}", toUserId);
            } catch (IOException e) {
                log.error("发送视频通话结束通知失败", e);
            }
        }
    }

    private void handleRegister(JSONObject message, Session session) {
        String userId = message.getStr("userId");
        userToSessionMap.put(userId, session.getId());
        sessionToUserMap.put(session.getId(), userId);

        // 发送注册成功响应
        JSONObject response = new JSONObject();
        response.set("type", "registerResponse");
        response.set("status", "success");

        sendMessage(session, response.toString());

        // 广播用户上线状态
        broadcastUserStatus(userId, "online");
    }

    private void handleOffer(JSONObject message, Session session) {
        String toUserId = message.getStr("to");
        String fromUserId = sessionToUserMap.get(session.getId());

        if (fromUserId == null) {
            log.warn("Unregistered session trying to send offer");
            return;
        }

        Session targetSession = sessions.get(userToSessionMap.get(toUserId));
        if (targetSession != null) {
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.set("type", "offer");
            forwardMessage.set("from", fromUserId);
            forwardMessage.set("offer", message.get("offer"));

            sendMessage(targetSession, forwardMessage.toString());
        }
    }

    private void handleAnswer(JSONObject message, Session session) {
        String toUserId = message.getStr("to");
        String fromUserId = sessionToUserMap.get(session.getId());

        if (fromUserId == null) {
            log.warn("Unregistered session trying to send answer");
            return;
        }

        Session targetSession = sessions.get(userToSessionMap.get(toUserId));
        if (targetSession != null) {
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.set("type", "answer");
            forwardMessage.set("from", fromUserId);
            forwardMessage.set("answer", message.get("answer"));

            sendMessage(targetSession, forwardMessage.toString());
        }
    }

    private void handleCandidate(JSONObject message, Session session) {
        String toUserId = message.getStr("to");
        String fromUserId = sessionToUserMap.get(session.getId());

        if (fromUserId == null) {
            log.warn("Unregistered session trying to send ICE candidate");
            return;
        }

        Session targetSession = sessions.get(userToSessionMap.get(toUserId));
        if (targetSession != null) {
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.set("type", "candidate");
            forwardMessage.set("from", fromUserId);
            forwardMessage.set("candidate", message.get("candidate"));

            sendMessage(targetSession, forwardMessage.toString());
        }
    }

    private void handleCall(JSONObject message, Session session) {
        String toUserId = message.getStr("to");
        String fromUserId = sessionToUserMap.get(session.getId());

        if (fromUserId == null) {
            log.warn("Unregistered session trying to initiate call");
            return;
        }

        Session targetSession = sessions.get(userToSessionMap.get(toUserId));
        if (targetSession != null) {
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.set("type", "incomingCall");
            forwardMessage.set("from", fromUserId);

            sendMessage(targetSession, forwardMessage.toString());
        }
    }

    private void handleEndCall(JSONObject message, Session session) {
        String toUserId = message.getStr("to");
        String fromUserId = sessionToUserMap.get(session.getId());

        if (fromUserId == null) {
            log.warn("Unregistered session trying to end call");
            return;
        }

        Session targetSession = sessions.get(userToSessionMap.get(toUserId));
        if (targetSession != null) {
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.set("type", "callEnded");
            forwardMessage.set("from", fromUserId);

            sendMessage(targetSession, forwardMessage.toString());
        }
    }

    private void broadcastUserStatus(String userId, String status) {
        JSONObject statusMessage = new JSONObject();
        statusMessage.set("type", "userStatus");
        statusMessage.set("userId", userId);
        statusMessage.set("status", status);

        sessions.values().forEach(s -> {
            if (s.isOpen()) {
                sendMessage(s, statusMessage.toString());
            }
        });
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }



    public void handlePrivateVideoChatMessage(JSONObject message) {

        handleVideoCallRequest(message);

    }

    public void handlePrivateVideoChatResponseMessage(JSONObject message) {
        System.out.println("响应视频通话请求");
        handleVideoCallResponse(message);
    }

    public void handlePrivateVideoChatEndMessage(JSONObject message) {
        System.out.println("通知对方通话已结束");
        handleVideoCallEnded(message);
    }


    public void handlePrivateMessage(JSONObject message) {
        JSONObject data = message.getJSONObject("data");
        Long fromUserId = data.getLong("from");
        Long toUserId = data.getLong("to");
        String content = data.getStr("content");
        String tempId = data.getStr("tempId");
        String fromUserNick = data.getStr("fromUserNick");

        Session senderSession = USER_SESSIONS.get(fromUserId);
        // 1. 验证发送者身份
        if (!senderSession.equals(USER_SESSIONS.get(fromUserId))) {
            log.warn("非法消息发送尝试，发送者身份:{}, session={}", fromUserId, senderSession.getId());
            return;
        }

        try {
            System.out.println("存储数据库之前："+message);
            // 2. 存储消息到数据库
            ChatMessage chatMessage = chatMessageService.savePrivateMessage(fromUserId, toUserId, content);

            // 3. 构造响应消息
            JSONObject response = new JSONObject();
            response.set("type", "PRIVATE_MESSAGE_ACK");


            JSONObject responseData = new JSONObject();
            responseData.set("id", chatMessage.getId());
            responseData.set("fromUserId", chatMessage.getFromUserId().toString());
            responseData.set("toUserId", chatMessage.getToUserId().toString());
            responseData.set("content", chatMessage.getContent());
            responseData.set("createTime", chatMessage.getCreateTime().getTime());
            responseData.set("status", 0);

            responseData.set("fromUserNick",fromUserNick);
            response.set("data", responseData);
            response.set("tempId", tempId);

            System.out.println("回执方之前："+responseData);
            // 4. 发送回执给发送方
            senderSession.getBasicRemote().sendText(response.toString());

            // 5. 准备转发消息（提前定义，避免作用域问题）
            JSONObject forwardMessage = new JSONObject();
            forwardMessage.set("type", "PRIVATE_MESSAGE");
            forwardMessage.set("data", responseData);

            String forwardMessageStr = forwardMessage.toString();

            // 6. 尝试转发给接收者
            Session receiverSession = USER_SESSIONS.get(toUserId);
            if (receiverSession != null && receiverSession.isOpen()) {   //在线发送给接收者
                System.out.println("发送给接收者："+forwardMessageStr);
                receiverSession.getBasicRemote().sendText(forwardMessageStr);
                //更新消息为已读状态
                chatMessageService.updateMessagesStatus(fromUserId, toUserId, 1);
            } else {                                                    //离线发送消息给接收者
                // 存储到Redis离线消息
                String key = RedisKeys.PRIVATE_MESSAGES + toUserId;
                redisUtil.lSet(key, forwardMessageStr);

            }
        } catch (Exception e) {
            log.error("处理私信失败", e);
            // 发送错误回执
            try {
                JSONObject errorResponse = new JSONObject();
                errorResponse.set("type", "PRIVATE_MESSAGE_ERROR");
                errorResponse.set("tempId", tempId);
                errorResponse.set("error", "消息发送失败");
                senderSession.getBasicRemote().sendText(errorResponse.toString());
            } catch (IOException ex) {
                log.error("发送错误回执失败", ex);
            }
        }
    }
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭WebSocket session失败", e);
        }
    }

    /**
     * 处理认证消息
     */
    private void handleAuth(String token, Session session) {

        if (StrUtil.isBlank(token)) {
            return;
        }
        try {
            // 验证token并获取用户ID
            Long userId = JwtUtil.getIdOfPayload(token);

            if (userId != null) {
                // 将用户ID和session关联存储
                USER_SESSIONS.put(userId, session);
                log.info("用户 {} 认证成功", userId);


            }
        } catch (Exception e) {
            log.error("认证失败", e);
        }
    }



    /**
     * 发送新视频通知给指定用户
     */
    public void sendNewVideoNotification(Long userId, String uploaderName, String videoTitle, Long videoId) {
        Session session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            JSONObject message = new JSONObject();
            message.set("type", "NEW_VIDEO");

            JSONObject data = new JSONObject();
            data.set("videoId", videoId);
            data.set("uploaderName", uploaderName);
            data.set("title", videoTitle);

            message.set("data", data);

            try {
                session.getBasicRemote().sendText(message.toString());
                log.info("发送新视频通知给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送消息失败", e);
            }
        }
    }

    /**
     * 批量发送新视频通知给多个用户
     */
    public void sendNewVideoNotificationBatch(List<Long> userIds, String uploaderName, VideoEntity videoEntity) {
        JSONObject data = new JSONObject();
        data.set("videoId", videoEntity.getId().toString());
        data.set("nick", uploaderName);
        data.set("title", videoEntity.getTitle());
        data.set("description", videoEntity.getDescription());

        JSONObject message = new JSONObject();
        message.set("type", "NEW_VIDEO");
        message.set("data", data);

        String messageText = message.toString();

        for (Long userId : userIds) {
            Session session = USER_SESSIONS.get(userId);
            // 如果用户在线，则发送消息
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageText);
                    log.info("发送新视频通知给用户 {}", userId);
                } catch (IOException e) {
                    log.error("发送消息给用户 {} 失败", userId, e);
                }
            } else {
                // 使用redisUtil获取列表
                log.info("用户 {} 不在线", userId);
                // 如果用户不在线，key: "video_notifications" + userId 则将消息存储到Redis中
                String key = RedisKeys.VIDEO_NOTIFICATIONS + userId;
                // 如果key不存在，则创建一个有序的列表
                if (!redisUtil.hasKey(key)) {
                    redisUtil.lSet(key, messageText);
                } else {
                    redisUtil.lSet(key, messageText);
                }
                log.info("将消息存储到Redis中: {}", messageText);
            }
        }
    }


    /**
     * 发送视频状态更新通知
     */
    public void sendVideoStatusUpdate(Long userId, Long videoId, VideoStatusEnum status, String message) {
        Session session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            JSONObject notification = new JSONObject();
            notification.set("type", "VIDEO_STATUS_UPDATE");

            JSONObject data = new JSONObject();
            data.set("videoId", videoId);
            data.set("status", status);
            data.set("message", message);

            notification.set("data", data);

            try {
                session.getBasicRemote().sendText(notification.toString());
                log.info("发送视频状态更新通知，userId: {}, videoId: {}, status: {}", userId, videoId, status);
            } catch (IOException e) {
                log.error("发送视频状态更新通知失败", e);
            }
        }
    }


    /**
     * 开播通知粉丝
     */
    public void notifyFans(List<Long> userIds,String publishId,String currentUserName) {
        JSONObject data = new JSONObject();
        data.set("roomId",publishId);

        System.out.println("notifyFans："+publishId);
        data.set("currentUserName",currentUserName);

        JSONObject message = new JSONObject();
        message.set("type", "NEW_Live");
        message.set("data", data);

        String messageText = message.toString();

        for (Long userId : userIds) {
            Session session = USER_SESSIONS.get(userId);
            // 如果用户在线，则发送消息
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageText);
                    log.info("通知粉丝 {} 开播了：",currentUserName);
                } catch (IOException e) {
                    log.error("通知粉丝开播了 {} 失败", userId, e);
                }
            }
        }
    }



    // 处理加入直播间
    private void handleJoinLiveRoom(JSONObject jsonMessage, Session session) {
        String roomId = jsonMessage.getStr("roomId");
        System.out.println("join roomId:"+roomId);
        Long userId = jsonMessage.getLong("userId");
        String userName = jsonMessage.getStr("userName");

        // 将用户添加到直播间

        Set<Long> users = LIVE_ROOM_USERS.computeIfAbsent(roomId,k -> ConcurrentHashMap.newKeySet());
        users.add(userId);

        // 广播用户加入消息
        JSONObject joinMessage = new JSONObject();
        joinMessage.set("type", "LIVE_USER_JOIN");
        joinMessage.set("roomId", roomId);
        joinMessage.set("userId", userId);
        joinMessage.set("userName", userName);
        joinMessage.set("time", System.currentTimeMillis());

        System.out.println("user:"+userName+":"+userId+":"+roomId);
        System.out.println(LIVE_ROOM_USERS);

        broadcastToLiveRoom(roomId, joinMessage.toString());
    }

    // 处理离开直播间
    private void handleLeaveLiveRoom(JSONObject jsonMessage) {
        String roomId = jsonMessage.getStr("roomId");
        Long userId = jsonMessage.getLong("userId");
        String username = jsonMessage.getStr("username");
        System.out.println("leave"+username);
        if (roomId != null && LIVE_ROOM_USERS.containsKey(roomId)) {
            LIVE_ROOM_USERS.get(roomId).remove(userId);

            // 广播用户离开消息
            JSONObject leaveMessage = new JSONObject();
            leaveMessage.set("type", "LIVE_USER_LEAVE");
            leaveMessage.set("roomId", roomId);
            leaveMessage.set("userId", userId);
            leaveMessage.set("username", username);
            leaveMessage.set("time", System.currentTimeMillis());

            broadcastToLiveRoom(roomId, leaveMessage.toString());
        }
    }

    // 处理直播间聊天消息
    private void handleLiveChatMessage(JSONObject jsonMessage) {
        String roomId = jsonMessage.getStr("roomId");
        Long userId = jsonMessage.getLong("userId");
        String userName = jsonMessage.getStr("userName");
        String text = jsonMessage.getStr("text");
        long time = jsonMessage.getLong("time");

        // 验证用户是否在直播间
        if (!LIVE_ROOM_USERS.containsKey(roomId) || !LIVE_ROOM_USERS.get(roomId).contains(userId)) {
            log.warn("用户 {} 不在直播间 {} 中，无法发送消息", userId, roomId);
            return;
        }


        // 构造聊天消息
        JSONObject chatMessage = new JSONObject();
        chatMessage.set("type", "LIVE_CHAT_MESSAGE");
        chatMessage.set("roomId", roomId);
        chatMessage.set("userId", userId.toString());
        chatMessage.set("userName", userName);
        chatMessage.set("text", text);
        chatMessage.set("time", time);

        // 广播到直播间
        broadcastToLiveRoom(roomId, chatMessage.toString());

        // 可以在这里将消息存储到数据库
        // chatMessageService.saveLiveChatMessage(roomId, userId, userName, text);
    }

    // 处理直播间礼物消息
    private void handleLiveGiftMessage(JSONObject jsonMessage) {
        String roomId = jsonMessage.getStr("roomId");
        Long userId = jsonMessage.getLong("userId");
        String userName = jsonMessage.getStr("userName");
        Long giftId = jsonMessage.getLong("giftId");
        String giftName = jsonMessage.getStr("giftName");
        Integer giftPrice = jsonMessage.getInt("giftPrice");
        long time = jsonMessage.getLong("time");

        // 验证用户是否在直播间
        if (!LIVE_ROOM_USERS.containsKey(roomId) || !LIVE_ROOM_USERS.get(roomId).contains(userId)) {
            log.warn("用户 {} 不在直播间 {} 中，无法发送礼物", userId, roomId);
            return;
        }

        // 构造礼物消息
        JSONObject giftMessage = new JSONObject();
        giftMessage.set("type", "LIVE_GIFT_MESSAGE");
        giftMessage.set("roomId", roomId);
        giftMessage.set("userId", userId);
        giftMessage.set("userName", userName);
        giftMessage.set("giftId", giftId);
        giftMessage.set("giftName", giftName);
        giftMessage.set("giftPrice", giftPrice);
        giftMessage.set("time", time);

        // 广播到直播间
        broadcastToLiveRoom(roomId, giftMessage.toString());

        // 可以在这里处理礼物逻辑，如扣除用户金币等
        // giftService.processGift(userId, giftId, roomId);
    }

    // 广播消息到直播间
    private void broadcastToLiveRoom(String roomId, String message) {
        if (LIVE_ROOM_USERS.containsKey(roomId)) {
            LIVE_ROOM_USERS.get(roomId).forEach(userId -> {
                Session session = USER_SESSIONS.get(userId);
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        log.error("发送消息到用户 {} 失败", userId, e);
                    }
                }
            });
        }
    }



    // 检测用户是否在线
    public static boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }

        // 检查用户会话是否存在且连接是打开的
        Session session = USER_SESSIONS.get(userId);
        System.out.println("toUserId"+userId+"session"+session);
        return session != null && session.isOpen();
    }

}
