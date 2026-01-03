package com.hsx.manyue.modules.im.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.im.mapper.IMGroupMapper;
import com.hsx.manyue.modules.im.mapper.IMGroupMemberMapper;
import com.hsx.manyue.modules.im.model.IMMessage;
import com.hsx.manyue.modules.im.model.entity.IMGroupEntity;
import com.hsx.manyue.modules.im.model.entity.IMGroupMemberEntity;
import com.hsx.manyue.modules.im.server.SessionManager;
import com.hsx.manyue.modules.im.service.IIMConversationService;
import com.hsx.manyue.modules.im.service.IIMGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import com.hsx.manyue.modules.im.service.IIMMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * IM群组服务实现
 */
@Slf4j
@Service
public class IMGroupServiceImpl extends ServiceImpl<IMGroupMapper, IMGroupEntity> 
        implements IIMGroupService {

    @Autowired
    private IMGroupMemberMapper groupMemberMapper;
    @Autowired
    private IIMConversationService conversationService;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    @Lazy
    private com.hsx.manyue.modules.im.service.IIMMessageService messageService;
    @Autowired
    @Lazy
    private com.hsx.manyue.modules.oss.service.IOssService ossService;

    @Override
    @Transactional
    public IMGroupEntity createGroup(Long ownerId, String groupName, String groupAvatar) {
        // 创建群组
        IMGroupEntity group = new IMGroupEntity();
        group.setId(IdUtil.getSnowflakeNextId());
        group.setGroupName(groupName);
        group.setGroupAvatar(groupAvatar);
        group.setOwnerId(ownerId);
        group.setGroupType(1); // 普通群
        group.setMemberCount(1);
        group.setMaxMemberCount(500);
        group.setMuteAll(0);
        group.setStatus(1);
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        group.setIsDelete(0);
        this.save(group);
        
        // 添加群主为成员
        IMGroupMemberEntity member = new IMGroupMemberEntity();
        member.setGroupId(group.getId());
        member.setUserId(ownerId);
        member.setMemberRole(1); // 群主
        member.setJoinTime(new Date());
        member.setIsDelete(0);
        groupMemberMapper.insert(member);
        
        // 创建会话
        conversationService.updateOrCreateConversation(ownerId, 2, group.getId(), "你创建了群组", null);

        // 发送群组通知
        sendGroupNotify(group.getId(), ownerId, "CREATE", "你创建了群组");

        log.info("创建群组成功: groupId={}, ownerId={}, groupName={}", group.getId(), ownerId, groupName);
        return group;
    }

    @Override
    @Transactional
    public void dissolveGroup(Long groupId, Long userId) {
        // 检查权限
        IMGroupEntity group = this.getById(groupId);
        if (group == null || !group.getOwnerId().equals(userId)) {
            throw new RuntimeException("无权解散群组");
        }
        
        // 更新群组状态
        group.setStatus(0); // 已解散
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        // 删除所有群成员
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId);
        groupMemberMapper.delete(wrapper);
        
        log.info("解散群组成功: groupId={}, userId={}", groupId, userId);
    }

    @Override
    @Transactional
    public void addMember(Long groupId, Long userId, Long inviterId) {
        // 检查群组是否存在
        IMGroupEntity group = this.getById(groupId);
        if (group == null || group.getStatus() == 0) {
            throw new RuntimeException("群组不存在或已解散");
        }
        
        // 检查是否已是成员
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
               .eq(IMGroupMemberEntity::getUserId, userId);
        if (groupMemberMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户已是群成员");
        }
        
        // 添加成员
        IMGroupMemberEntity member = new IMGroupMemberEntity();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setMemberRole(3); // 普通成员
        member.setJoinTime(new Date());
        member.setIsDelete(0);
        groupMemberMapper.insert(member);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() + 1);
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        // 创建会话
        conversationService.updateOrCreateConversation(userId, 2, groupId, "你加入了群组", null);

        // 发送通知给所有群成员
        sendGroupNotify(groupId, userId, "JOIN", "用户 " + userId + " 加入了群组");

        log.info("添加群成员成功: groupId={}, userId={}, inviterId={}", groupId, userId, inviterId);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId, Long operatorId) {
        // 检查权限(群主或管理员)
        // TODO: 权限检查
        
        // 删除成员
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
               .eq(IMGroupMemberEntity::getUserId, userId);
        groupMemberMapper.delete(wrapper);
        
        // 更新群成员数
        IMGroupEntity group = this.getById(groupId);
        group.setMemberCount(group.getMemberCount() - 1);
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        log.info("移除群成员成功: groupId={}, userId={}, operatorId={}", groupId, userId, operatorId);
    }

    @Override
    public void updateGroupInfo(Long groupId, Long operatorId, String groupName, String groupAvatar,
                                String introduction, String notification) {
        // 检查权限（群主或管理员）
        IMGroupEntity group = this.getById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        // 检查操作者权限
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
               .eq(IMGroupMemberEntity::getUserId, operatorId);
        IMGroupMemberEntity member = groupMemberMapper.selectOne(wrapper);
        
        if (member == null || (member.getMemberRole() != 1 && member.getMemberRole() != 2)) {
            throw new RuntimeException("只有群主或管理员可以修改群信息");
        }
        
        // 更新群组信息
        if (groupName != null && !groupName.isEmpty()) {
            group.setGroupName(groupName);
        }
        if (groupAvatar != null && !groupAvatar.isEmpty()) {
            group.setGroupAvatar(groupAvatar);
        }
        if (introduction != null) {
            group.setIntroduction(introduction);
        }
        if (notification != null) {
            group.setNotification(notification);
        }
        
        group.setUpdateTime(new Date());
        this.updateById(group);
        log.info("更新群信息成功: groupId={}, operatorId={}", groupId, operatorId);
    }

    @Override
    public List<IMGroupMemberEntity> getGroupMembers(Long groupId) {
        return groupMemberMapper.queryGroupMembersWithUserInfo(groupId);
    }

    @Override
    public List<Long> getGroupMemberIds(Long groupId) {
        return groupMemberMapper.queryGroupMemberIds(groupId);
    }

    @Override
    public List<IMGroupEntity> getUserGroups(Long userId) {
        return baseMapper.queryUserGroups(userId);
    }

    @Override
    @Transactional
    public void inviteMember(Long groupId, Long userId, Long inviterId) {
        // 检查群组是否存在
        IMGroupEntity group = this.getById(groupId);
        if (group == null || group.getStatus() == 0) {
            throw new RuntimeException("群组不存在或已解散");
        }
        
        // 检查邀请者是否是群成员
        LambdaQueryWrapper<IMGroupMemberEntity> inviterWrapper = new LambdaQueryWrapper<>();
        inviterWrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
                .eq(IMGroupMemberEntity::getUserId, inviterId)
                .eq(IMGroupMemberEntity::getIsDelete, 0);
        if (groupMemberMapper.selectCount(inviterWrapper) == 0) {
            throw new RuntimeException("您不是群成员，无法邀请他人");
        }
        
        // 检查被邀请者是否已是成员
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
                .eq(IMGroupMemberEntity::getUserId, userId)
                .eq(IMGroupMemberEntity::getIsDelete, 0);
        if (groupMemberMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户已是群成员");
        }
        
        // 直接添加成员（简化版，不需要对方同意）
        IMGroupMemberEntity member = new IMGroupMemberEntity();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setMemberRole(3); // 普通成员
        member.setJoinTime(new Date());
        member.setIsDelete(0);
        groupMemberMapper.insert(member);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() + 1);
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        // 创建会话
        conversationService.updateOrCreateConversation(userId, 2, groupId, "你被邀请加入了群组", null);

        // 发送通知给所有群成员
        sendGroupNotify(groupId, userId, "INVITED", "用户 " + inviterId + " 邀请 " + userId + " 加入了群组");

        log.info("邀请群成员成功: groupId={}, userId={}, inviterId={}", groupId, userId, inviterId);
        
        // TODO: 发送群组邀请通知到被邀请人（通过WebSocket）
        // 可以使用SessionManager发送通知
    }

    @Override
    public String uploadGroupAvatar(Long groupId, Long operatorId, org.springframework.web.multipart.MultipartFile file) {
        // 检查权限（群主或管理员）
        IMGroupEntity group = this.getById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        // 检查操作者权限
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
               .eq(IMGroupMemberEntity::getUserId, operatorId);
        IMGroupMemberEntity member = groupMemberMapper.selectOne(wrapper);
        
        if (member == null || (member.getMemberRole() != 1 && member.getMemberRole() != 2)) {
            throw new RuntimeException("只有群主或管理员可以修改群头像");
        }
        
        // 使用OSS服务上传文件
        try {
            String avatarUrl = ossService.uploadFile(file);
            
            // 更新群组头像
            group.setGroupAvatar(avatarUrl);
            group.setUpdateTime(new Date());
            this.updateById(group);
            
            log.info("上传群组头像成功: groupId={}, avatarUrl={}", groupId, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("上传群组头像失败", e);
            throw new RuntimeException("上传群组头像失败: " + e.getMessage());
        }
    }

    /**
     * 发送群组通知给所有活跃成员
     */
    private void sendGroupNotify(Long groupId, Long targetUserId, String action, String content) {
        // 1. WebSocket实时推送通知
        IMMessage notify = new IMMessage();
        notify.setMsgType(IMMessage.TYPE_GROUP_NOTIFY);
        notify.setToGroupId(groupId);
        notify.setContent(action);
        notify.setMsgTime(System.currentTimeMillis());
        notify.setData(content); // 携带具体描述
        
        List<Long> memberIds = this.getGroupMemberIds(groupId);
        for (Long memberId : memberIds) {
            sessionManager.pushMessage(memberId, notify);
        }

        // 2. 将通知作为系统消息存入聊天记录
        try {
            com.hsx.manyue.modules.im.model.dto.IMMessageDTO dto = new com.hsx.manyue.modules.im.model.dto.IMMessageDTO();
            dto.setFromUserId(0L); // 系统用户
            dto.setToGroupId(groupId);
            dto.setContent(content);
            dto.setContentType(1); // 文本
            dto.setClientMsgId(IdUtil.fastSimpleUUID());
            
            // 使用 messageService 持久化并推送
            messageService.sendGroupMessage(dto); 
        } catch (Exception e) {
            log.error("保存系统消息失败", e);
        }
    }
    
    @Override
    @Transactional
    public void exitGroup(Long groupId, Long userId) {
        // 检查群组是否存在
        IMGroupEntity group = this.getById(groupId);
        if (group == null || group.getStatus() == 0) {
            throw new RuntimeException("群组不存在或已解散");
        }
        
        // 检查是否是群主（群主不能退出，只能解散）
        if (group.getOwnerId().equals(userId)) {
            throw new RuntimeException("群主不能退出群组，请解散群组");
        }
        
        // 检查是否是群成员
        LambdaQueryWrapper<IMGroupMemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMGroupMemberEntity::getGroupId, groupId)
               .eq(IMGroupMemberEntity::getUserId, userId);
        IMGroupMemberEntity member = groupMemberMapper.selectOne(wrapper);
        
        if (member == null) {
            throw new RuntimeException("您不是群成员");
        }
        
        // 删除成员记录
        groupMemberMapper.delete(wrapper);
        
        // 更新群成员数
        group.setMemberCount(group.getMemberCount() - 1);
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        // 发送通知给其他群成员
        sendGroupNotify(groupId, userId, "EXIT", "用户 " + userId + " 退出了群组");
        
        log.info("用户退出群组成功: groupId={}, userId={}", groupId, userId);
    }
}

