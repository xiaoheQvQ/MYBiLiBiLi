package com.hsx.manyue.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.common.dto.Token;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.user.model.params.UserParam;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;

/**
 * 用户表 服务类
 */
public interface IUserService extends IService<UserEntity> {

    /**
     * 注册用户
     *
     * @param user
     */
    void signup(UserParam user);

    /**
     * 向邮箱发送验证码
     *
     * @param email
     */
    void sendCaptcha(String email);

    /**
     * 登录并返回token
     *
     * @param user
     * @return
     */
    Token login(UserParam user);

    /**
     * 生成 token
     *
     * @param id
     * @return
     */
    Token generateToken(Long id);

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return
     */
    Token refreshToken(String refreshToken);

    UserDTO getInfoById(Long userId);

    void updateInfo(UserParam param);

    IPage<UserDTO> getPage(VideoQueryParam param);
}
