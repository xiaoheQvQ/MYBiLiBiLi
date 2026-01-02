package com.hsx.manyue.modules.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.modules.im.mapper.IMFriendApplyMapper;
import com.hsx.manyue.modules.im.mapper.IMFriendMapper;
import com.hsx.manyue.modules.im.model.IMMessage;
import com.hsx.manyue.modules.im.model.entity.IMFriendApplyEntity;
import com.hsx.manyue.modules.im.model.entity.IMFriendEntity;
import com.hsx.manyue.modules.im.server.SessionManager;
import com.hsx.manyue.modules.im.service.IIMFriendService;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * IM好友服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IMFriendServiceImpl extends ServiceImpl<IMFriendMapper, IMFriendEntity> 
        implements IIMFriendService {

    private final IMFriendApplyMapper friendApplyMapper;
    private final SessionManager sessionManager;
    private final IUserService userService;

    @Override
    @Transactional
    public void applyFriend(Long fromUserId, Long toUserId, String applyMsg) {
        // 检查是否已是好友
        if (isFriend(fromUserId, toUserId)) {
            throw new RuntimeException("已经是好友");
        }
        
        // 检查是否已有待处理的申请
        LambdaQueryWrapper<IMFriendApplyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMFriendApplyEntity::getFromUserId, fromUserId)
               .eq(IMFriendApplyEntity::getToUserId, toUserId)
               .eq(IMFriendApplyEntity::getStatus, 0);
        if (friendApplyMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已有待处理的好友申请");
        }
        
        // 创建好友申请
        IMFriendApplyEntity apply = new IMFriendApplyEntity();
        apply.setFromUserId(fromUserId);
        apply.setToUserId(toUserId);
        apply.setApplyMsg(applyMsg);
        apply.setStatus(0); // 待处理
        apply.setCreateTime(new Date());
        apply.setUpdateTime(new Date());
        friendApplyMapper.insert(apply);
        
        // 发送好友申请通知
        IMMessage message = new IMMessage();
        message.setMsgType(IMMessage.TYPE_FRIEND_APPLY);
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(applyMsg);
        message.setMsgTime(System.currentTimeMillis());
        sessionManager.pushMessage(toUserId, message);
        
        log.info("好友申请成功: from={}, to={}", fromUserId, toUserId);
    }

    @Override
    @Transactional
    public void acceptFriend(Long applyId, Long userId) {
        // 查询申请
        IMFriendApplyEntity apply = friendApplyMapper.selectById(applyId);
        if (apply == null || !apply.getToUserId().equals(userId)) {
            throw new RuntimeException("申请不存在或无权操作");
        }
        
        // 更新申请状态
        apply.setStatus(1); // 已同意
        apply.setUpdateTime(new Date());
        friendApplyMapper.updateById(apply);
        
        // 双向添加好友关系
        addFriendRelation(apply.getFromUserId(), apply.getToUserId());
        addFriendRelation(apply.getToUserId(), apply.getFromUserId());
        
        // 推送通知给申请人(A)
        IMMessage notifyMsg = new IMMessage();
        notifyMsg.setMsgType(IMMessage.TYPE_FRIEND_APPLY);
        notifyMsg.setFromUserId(userId);
        notifyMsg.setToUserId(apply.getFromUserId());
        notifyMsg.setContent("ACCEPTED"); // 标记为已通过
        notifyMsg.setMsgTime(System.currentTimeMillis());
        sessionManager.pushMessage(apply.getFromUserId(), notifyMsg);
        
        log.info("同意好友申请: applyId={}, userId={}", applyId, userId);
    }

    @Override
    @Transactional
    public void rejectFriend(Long applyId, Long userId) {
        // 查询申请
        IMFriendApplyEntity apply = friendApplyMapper.selectById(applyId);
        if (apply == null || !apply.getToUserId().equals(userId)) {
            throw new RuntimeException("申请不存在或无权操作");
        }
        
        // 更新申请状态
        apply.setStatus(2); // 已拒绝
        apply.setUpdateTime(new Date());
        friendApplyMapper.updateById(apply);
        
        log.info("拒绝好友申请: applyId={}, userId={}", applyId, userId);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        // 双向删除好友关系
        deleteFriendRelation(userId, friendId);
        deleteFriendRelation(friendId, userId);
        
        log.info("删除好友成功: userId={}, friendId={}", userId, friendId);
    }

    @Override
    @Transactional
    public void blockFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<IMFriendEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMFriendEntity::getUserId, userId)
               .eq(IMFriendEntity::getFriendId, friendId);
        
        IMFriendEntity friend = this.getOne(wrapper);
        if (friend != null) {
            friend.setStatus(2); // 拉黑
            friend.setUpdateTime(new Date());
            this.updateById(friend);
            log.info("拉黑好友成功: userId={}, friendId={}", userId, friendId);
        }
    }

    @Override
    public List<IMFriendEntity> getFriendList(Long userId) {
        LambdaQueryWrapper<IMFriendEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMFriendEntity::getUserId, userId)
               .eq(IMFriendEntity::getStatus, 1) // 正常状态
               .eq(IMFriendEntity::getIsDelete, 0);
        List<IMFriendEntity> list = this.list(wrapper);
        for (IMFriendEntity friend : list) {
            UserEntity user = userService.getById(friend.getFriendId());
            if (user != null) {
                friend.setNick(user.getNick());
                friend.setAvatar(user.getAvatar());
            }
        }
        return list;
    }

    @Override
    public List<IMFriendApplyEntity> getFriendApplyList(Long userId) {
        LambdaQueryWrapper<IMFriendApplyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMFriendApplyEntity::getToUserId, userId)
               .eq(IMFriendApplyEntity::getStatus, 0) // 待处理
               .orderByDesc(IMFriendApplyEntity::getCreateTime);
        List<IMFriendApplyEntity> list = friendApplyMapper.selectList(wrapper);
        for (IMFriendApplyEntity apply : list) {
            UserEntity user = userService.getById(apply.getFromUserId());
            if (user != null) {
                apply.setNick(user.getNick());
                apply.setAvatar(user.getAvatar());
            }
        }
        return list;
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<IMFriendEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMFriendEntity::getUserId, userId)
               .eq(IMFriendEntity::getFriendId, friendId)
               .eq(IMFriendEntity::getStatus, 1)
               .eq(IMFriendEntity::getIsDelete, 0);
        return this.count(wrapper) > 0;
    }

    /**
     * 添加好友关系
     */
    private void addFriendRelation(Long userId, Long friendId) {
        IMFriendEntity friend = new IMFriendEntity();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(1); // 正常
        friend.setCreateTime(new Date());
        friend.setUpdateTime(new Date());
        friend.setIsDelete(0);
        this.save(friend);
    }

    /**
     * 删除好友关系
     */
    private void deleteFriendRelation(Long userId, Long friendId) {
        LambdaQueryWrapper<IMFriendEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IMFriendEntity::getUserId, userId)
               .eq(IMFriendEntity::getFriendId, friendId);
        this.remove(wrapper);
    }
}
