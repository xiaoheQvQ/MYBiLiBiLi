package com.hsx.manyue.modules.chat.controller;

import cn.hutool.json.JSONObject;
import com.hsx.manyue.common.annotation.aspect.ApiLimit;
import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.DeepSeekAiUtil;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.chat.model.dto.ChatMessageDTO;
import com.hsx.manyue.modules.chat.service.IChatMessageService;
import com.hsx.manyue.modules.chat.service.impl.ChatMessageServiceImpl;
import com.hsx.manyue.modules.notification.WebSocketServer;
import com.hsx.manyue.modules.user.service.impl.UserSubscriptionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.SocketTimeoutException;
import java.util.List;

/**
 * AI聊天控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@ApiLimit
@Tag(name = "AI聊天接口")
public class ChatRestController extends SuperController {

    private final DeepSeekAiUtil deepSeekAiUtil;

    @PostMapping("/message")
    @Operation(summary = "发送AI聊天消息")
    @Login
    public R sendMessage(@RequestBody ChatMessageDTO message) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String response = deepSeekAiUtil.doChat(userId, message.getContent());
        return success(response);
    }

    @PostMapping("/clear")
    @Operation(summary = "清除聊天历史")
    @Login
    public R clearChatHistory() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        deepSeekAiUtil.clearChatHistory(userId);
        return R.success();
    }

    @PostMapping("/new-session")
    @Operation(summary = "创建新的聊天会话")
    @Login
    public R createNewSession() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        deepSeekAiUtil.clearChatHistory(userId);
        deepSeekAiUtil.createNewChatSession(userId);
        return R.success();
    }

    @Autowired
    private ChatMessageServiceImpl chatMessageService;

    @Autowired
    private UserSubscriptionServiceImpl userSubscriptionService;

    @Operation(summary = "获取聊天历史")
    @GetMapping("/message/history")
    public R getChatHistory(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam(defaultValue = "50") int limit) {
        return R.success(
                chatMessageService.getChatHistory(fromUserId, toUserId, limit)
        );
    }

    @Operation(summary = "发送私信")
    @PostMapping("/message/send")
    public R ChatSend(@RequestBody JSONObject message) {
        System.out.println("发送的私信："+message);
        userSubscriptionService.ChatSend(message);
        return R.success();
    }

    @Operation(summary = "发送视频通话请求")
    @PostMapping("/videoChatMessage/send")
    public R videoChatSend(@RequestBody JSONObject message) {
        System.out.println("发送的私信："+message);
        userSubscriptionService.VideoChatSend(message);
        return R.success();
    }

    @Operation(summary = "发送视频通话响应")
    @PostMapping("/videoChatResponseMessage/send")
    public R videoChatResponseSend(@RequestBody JSONObject message) {
        System.out.println("发送的私信："+message);
        userSubscriptionService.VideoChatResponseSend(message);
        return R.success();
    }


    @Operation(summary = "通知对方通话已结束")
    @PostMapping("/videoChatEndmessage/send")
    public R sendPrivateVideoChatEndMessage(@RequestBody JSONObject message) {
        System.out.println("发送的私信："+message);
        userSubscriptionService.sendPrivateVideoChatEndMessage(message);
        return R.success();
    }



    @Operation(summary = "标记消息为已读")
    @PostMapping("/message/markAsRead")
    public R  markAsRead(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId) {
        return R.success(
                chatMessageService.updateMessagesStatus(fromUserId, toUserId, 1)
        );
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/message/unreadCount")
    public R countUnreadMessages(@RequestParam Long userId) {
        return R.success(
                chatMessageService.countUnreadMessages(userId)
        );
    }


    @Operation(summary = "检查用户是否在线")
    @GetMapping("/online")
    public R isUserOnline(@RequestParam Long userId) {
        boolean isOnline = WebSocketServer.isUserOnline(userId);
        System.out.println("isOnline"+isOnline);
        return R.success(isOnline);
    }

}
