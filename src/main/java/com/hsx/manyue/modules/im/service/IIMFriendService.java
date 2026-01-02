package com.hsx.manyue.modules.im.service;

import com.hsx.manyue.modules.im.model.entity.IMFriendApplyEntity;
import com.hsx.manyue.modules.im.model.entity.IMFriendEntity;

import java.util.List;

/**
 * IM好友服务接口
 */
public interface IIMFriendService {
    
    /**
     * 申请添加好友
     */
    void applyFriend(Long fromUserId, Long toUserId, String applyMsg);
    
    /**
     * 同意好友申请
     */
    void acceptFriend(Long applyId, Long userId);
    
    /**
     * 拒绝好友申请
     */
    void rejectFriend(Long applyId, Long userId);
    
    /**
     * 删除好友
     */
    void deleteFriend(Long userId, Long friendId);
    
    /**
     * 拉黑好友
     */
    void blockFriend(Long userId, Long friendId);
    
    /**
     * 获取好友列表
     */
    List<IMFriendEntity> getFriendList(Long userId);
    
    /**
     * 获取好友申请列表
     */
    List<IMFriendApplyEntity> getFriendApplyList(Long userId);
    
    /**
     * 检查是否是好友
     */
    boolean isFriend(Long userId, Long friendId);
}
