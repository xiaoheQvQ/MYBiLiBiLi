package com.hsx.manyue.modules.im.controller;

import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.entity.IMGroupEntity;
import com.hsx.manyue.modules.im.model.entity.IMGroupMemberEntity;
import com.hsx.manyue.modules.im.service.IIMGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM群组控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/im/group")
@Tag(name = "IM群组接口")
public class IMGroupController extends SuperController {

    private final IIMGroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建群组")
    @Login
    public R createGroup(@RequestParam String groupName,
                         @RequestParam(required = false) String groupAvatar) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        IMGroupEntity group = groupService.createGroup(userId, groupName, groupAvatar);
        return success(group);
    }

    @PostMapping("/dissolve")
    @Operation(summary = "解散群组")
    @Login
    public R dissolveGroup(@RequestParam Long groupId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        groupService.dissolveGroup(groupId, userId);
        return success();
    }

    @PostMapping("/member/add")
    @Operation(summary = "添加群成员")
    @Login
    public R addMember(@RequestParam Long groupId,
                       @RequestParam Long targetUserId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        groupService.addMember(groupId, targetUserId, userId);
        return success();
    }

    @PostMapping("/invite")
    @Operation(summary = "邀请用户加入群组")
    @Login
    public R inviteMember(@RequestParam Long groupId,
                          @RequestParam Long targetUserId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        groupService.inviteMember(groupId, targetUserId, userId);
        return success();
    }

    @PostMapping("/member/remove")
    @Operation(summary = "移除群成员")
    @Login
    public R removeMember(@RequestParam Long groupId,
                          @RequestParam Long targetUserId) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        groupService.removeMember(groupId, targetUserId, userId);
        return success();
    }

    @GetMapping("/members")
    @Operation(summary = "获取群成员列表")
    @Login
    public R getGroupMembers(@RequestParam Long groupId) {
        List<IMGroupMemberEntity> members = groupService.getGroupMembers(groupId);
        return success(members);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户群组列表")
    @Login
    public R getGroupList() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMGroupEntity> list = groupService.getUserGroups(userId);
        return success(list);
    }

    @PostMapping("/updateInfo")
    @Operation(summary = "更新群组信息")
    @Login
    public R updateGroupInfo(@RequestParam Long groupId,
                             @RequestParam(required = false) String groupName,
                             @RequestParam(required = false) String groupAvatar,
                             @RequestParam(required = false) String introduction,
                             @RequestParam(required = false) String notification) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        groupService.updateGroupInfo(groupId, userId, groupName, groupAvatar, introduction, notification);
        return success();
    }

    @PostMapping("/uploadAvatar")
    @Operation(summary = "上传群组头像")
    @Login
    public R uploadAvatar(@RequestParam Long groupId,
                          @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String avatarUrl = groupService.uploadGroupAvatar(groupId, userId, file);
        return success(avatarUrl);
    }
}
