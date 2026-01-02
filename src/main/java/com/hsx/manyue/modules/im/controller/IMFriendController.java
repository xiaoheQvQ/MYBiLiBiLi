package com.hsx.manyue.modules.im.controller;

import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.entity.IMFriendApplyEntity;
import com.hsx.manyue.modules.im.model.entity.IMFriendEntity;
import com.hsx.manyue.modules.im.service.IIMFriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM好友控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/im/friend")
@Tag(name = "IM好友接口")
public class IMFriendController extends SuperController {

    private final IIMFriendService friendService;

    @PostMapping("/apply")
    @Operation(summary = "申请添加好友")
    @Login
    public R applyFriend(@RequestParam Long friendId,
                         @RequestParam(required = false) String applyMsg) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        friendService.applyFriend(userId, friendId, applyMsg);
        return success();
    }

    @PostMapping("/apply/accept")
    @Operation(summary = "同意好友申请")
    @Login
    public R acceptFriend(@RequestParam Long applyId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        friendService.acceptFriend(applyId, userId);
        return success();
    }

    @PostMapping("/apply/reject")
    @Operation(summary = "拒绝好友申请")
    @Login
    public R rejectFriend(@RequestParam Long applyId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        friendService.rejectFriend(applyId, userId);
        return success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除好友")
    @Login
    public R deleteFriend(@RequestParam Long friendId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        friendService.deleteFriend(userId, friendId);
        return success();
    }

    @PostMapping("/block")
    @Operation(summary = "拉黑好友")
    @Login
    public R blockFriend(@RequestParam Long friendId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        friendService.blockFriend(userId, friendId);
        return success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取好友列表")
    @Login
    public R getFriendList() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMFriendEntity> friends = friendService.getFriendList(userId);
        return success(friends);
    }

    @GetMapping("/apply/list")
    @Operation(summary = "获取好友申请列表")
    @Login
    public R getFriendApplyList() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMFriendApplyEntity> applies = friendService.getFriendApplyList(userId);
        return success(applies);
    }
}
