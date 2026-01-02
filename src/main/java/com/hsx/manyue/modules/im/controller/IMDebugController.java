package com.hsx.manyue.modules.im.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.modules.im.mapper.*;
import com.hsx.manyue.modules.im.model.entity.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * IM 调试控制器 - 仅用于开发调试，查看数据库数据
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/im/debug")
@Tag(name = "IM调试接口")
public class IMDebugController {

    private final IMGroupMapper groupMapper;
    private final IMGroupMemberMapper groupMemberMapper;
    private final IMConversationMapper conversationMapper;
    private final IMMessageMapper messageMapper;

    @GetMapping("/groups")
    @Operation(summary = "查看所有群组")
    public R getAllGroups() {
        return R.success( groupMapper.selectList(null));
    }

    @GetMapping("/group/members")
    @Operation(summary = "查看群成员")
    public R getGroupMembers(@RequestParam Long groupId) {
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId);
        return R.success( groupMemberMapper.selectList(wrapper));
    }

    @GetMapping("/conversations")
    @Operation(summary = "查看用户会话")
    public R getConversations(@RequestParam Long userId) {
        LambdaQueryWrapper<IMConversationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMConversationEntity::getUserId, userId);
        return R.success( conversationMapper.selectList(wrapper));
    }

    @GetMapping("/messages/group")
    @Operation(summary = "查看群消息")
    public R getGroupMessages(@RequestParam Long groupId) {
        LambdaQueryWrapper<IMMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMMessageEntity::getToGroupId, groupId);
        return R.success( messageMapper.selectList(wrapper));
    }
}
