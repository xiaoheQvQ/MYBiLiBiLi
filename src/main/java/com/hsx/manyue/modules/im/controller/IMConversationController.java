package com.hsx.manyue.modules.im.controller;

import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.entity.IMConversationEntity;
import com.hsx.manyue.modules.im.model.vo.IMConversationVO;
import com.hsx.manyue.modules.im.service.IIMConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM会话控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/im/conversation")
@Tag(name = "IM会话接口")
public class IMConversationController extends SuperController {

    private final IIMConversationService conversationService;

    @GetMapping("/list")
    @Operation(summary = "获取会话列表")
    @Login
    public R getConversationList() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMConversationVO> list = conversationService.getConversationList(userId);
        return success(list);
    }

    @PostMapping("/clearUnread")
    @Operation(summary = "清除未读数")
    @Login
    public R clearUnread(@RequestParam Integer conversationType,
                         @RequestParam Long targetId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        conversationService.clearUnread(userId, conversationType, targetId);
        return success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除会话")
    @Login
    public R deleteConversation(@RequestParam Integer conversationType,
                                @RequestParam Long targetId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        conversationService.deleteConversation(userId, conversationType, targetId);
        return success();
    }

    @PostMapping("/pin")
    @Operation(summary = "置顶/取消置顶会话")
    @Login
    public R pinConversation(@RequestParam Integer conversationType,
                             @RequestParam Long targetId,
                             @RequestParam Boolean isTop) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        conversationService.pinConversation(userId, conversationType, targetId, isTop);
        return success();
    }

    @PostMapping("/mute")
    @Operation(summary = "免打扰/取消免打扰")
    @Login
    public R muteConversation(@RequestParam Integer conversationType,
                              @RequestParam Long targetId,
                              @RequestParam Boolean isMute) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        conversationService.muteConversation(userId, conversationType, targetId, isMute);
        return success();
    }
}
