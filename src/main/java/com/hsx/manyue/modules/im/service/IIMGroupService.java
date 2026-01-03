package com.hsx.manyue.modules.im.service;

import com.hsx.manyue.modules.im.model.entity.IMGroupEntity;
import com.hsx.manyue.modules.im.model.entity.IMGroupMemberEntity;

import java.util.List;

/**
 * IM群组服务接口
 */
public interface IIMGroupService {
    
    /**
     * 创建群组
     */
    IMGroupEntity createGroup(Long ownerId, String groupName, String groupAvatar);
    
    /**
     * 解散群组
     */
    void dissolveGroup(Long groupId, Long userId);
    
    /**
     * 添加群成员
     */
    void addMember(Long groupId, Long userId, Long inviterId);
    
    /**
     * 移除群成员
     */
    void removeMember(Long groupId, Long userId, Long operatorId);

    
    /**
     * 获取群成员列表
     */
    List<IMGroupMemberEntity> getGroupMembers(Long groupId);

    /**
     * 获取用户加入的群组列表
     */
    List<IMGroupEntity> getUserGroups(Long userId);
    
    /**
     * 邀请用户加入群组
     */
    void inviteMember(Long groupId, Long userId, Long inviterId);
    
    /**
     * 更新群组信息（包括名称、头像、简介、公告）
     */
    void updateGroupInfo(Long groupId, Long operatorId, String groupName, String groupAvatar,
                         String introduction, String notification);

    List<Long> getGroupMemberIds(Long groupId);

    /**
     * 上传群组头像
     */
    String uploadGroupAvatar(Long groupId, Long operatorId, org.springframework.web.multipart.MultipartFile file);
    
    /**
     * 退出群组（用户主动退出）
     */
    void exitGroup(Long groupId, Long userId);
}
