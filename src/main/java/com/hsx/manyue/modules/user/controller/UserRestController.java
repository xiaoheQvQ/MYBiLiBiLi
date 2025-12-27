package com.hsx.manyue.modules.user.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.config.RsaProperty;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.dto.Token;
import com.hsx.manyue.common.enums.ReturnCodeEnums;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.chat.model.dto.ChatMessage;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.model.params.UserParam;
import com.hsx.manyue.modules.user.service.IRefreshTokenService;
import com.hsx.manyue.modules.user.service.IUserService;
import com.hsx.manyue.modules.user.service.IUserSubscriptionService;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserRestController extends SuperController {

    private final RsaProperty rsaProperty;
    private final IUserService userService;
    private final IRefreshTokenService refreshTokenService;
    private final IUserSubscriptionService userSubscriptionService;


    /**
     * 获取公钥
     *
     * @return
     */
    @GetMapping("/rsa-pks")
    public R getPublicKey() {
        return R.success(rsaProperty.getPublicKey());
    }

    /**
     * 加密内容 - 测试用
     *
     * @param str 待加密内容
     * @return
     */
    @GetMapping("/encrypt")
    public R encrypt(String str) {
        return R.success(rsaProperty.encryptByPublicKey(str));
    }

    /**
     * 解密内容 - 测试用
     *
     * @param str 待解密内容
     * @return
     */
    @PostMapping("/decrypt")
    public R decrypt(@RequestBody String str) {
        return R.success(rsaProperty.decryptByPrivateKey(str));
    }

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    @PostMapping("/signup")
    public R signup(@RequestBody @Validated(UserParam.Signup.class) UserParam user) {
        System.out.println(user.toString());
        userService.signup(user);
        return R.success();
    }

    /**
     * 用户登录
     *
     * @param user
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody @Validated(UserParam.Login.class) UserParam user) {
        Token token = userService.login(user);
        return R.success(token);
    }

    @GetMapping("/info")
    @Login
    public R getInfo() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        if (userId == null) {
            return R.failure(ReturnCodeEnums.NO_LOGIN);
        }
        UserDTO userDTO = userService.getInfoById(userId);
        return this.success(userDTO);
    }

    @GetMapping("/info/{userId}")
    public R getUserInfo(@PathVariable Long userId) {
        UserDTO userDTO = userService.getInfoById(userId);
        System.out.println("查询到的用户信息"+userDTO.toString());
        return this.success(userDTO);
    }

    @PutMapping("/info")
    @Login
    public R updateInfo(@ModelAttribute UserParam param) {
        System.out.println("准备更新的信息："+param.toString());
        userService.updateInfo(param);

        return success();
    }

    /**
     * 邮箱获取验证码
     *
     * @param email
     * @return
     */
    @PostMapping("/mail-captcha")
    public R captcha(String email) {
        userService.sendCaptcha(email);
        return R.success();
    }

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return
     */
    @PutMapping("/refresh-token")
    public R updateToken(@RequestParam String refreshToken) {
        Token token = userService.refreshToken(refreshToken);
        return success(token);
    }

    /**
     * 退出登录
     */
    @DeleteMapping("/logout")
    public R logout(@RequestHeader("accessToken") String accessToken) {
        refreshTokenService.logout(accessToken);
        return success();
    }

    @Login
    @PostMapping("/subscribe/{userId}")
    public R subscribe(@PathVariable Long userId) {
        Long currentUserId = JwtUtil.LOGIN_USER_HANDLER.get();
        Assert.isFalse(NumberUtil.equals(userId, currentUserId), "不能关注自己！");
        userSubscriptionService.subscribe(currentUserId, userId);
        return R.success();
    }

    @Login
    @DeleteMapping("/subscribe/{userId}")
    public R cancelSubscribe(@PathVariable Long userId) {
        Long currentUserId = JwtUtil.LOGIN_USER_HANDLER.get();
        userSubscriptionService.cancelSubscribe(currentUserId, userId);
        return R.success();
    }

    @Login
    @GetMapping("/subscribe/msg")
    public R getVideoMsg() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<VideoDTO> msg = userSubscriptionService.getMsg(userId);
        return success(msg);
    }

    @Login
    @GetMapping("/subscribe/Chatmsg")
    public R getChatMsg() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<ChatMessage> msg = userSubscriptionService.getChatMsg(userId);
        return success(msg);
    }

    @DeleteMapping("/subscribe/msg/{index}")
    @Login
    public R consumeMsg(@PathVariable Integer index) {
        System.out.println(" =============== ");
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        userSubscriptionService.consumeMsg(userId, index);
        return success();
    }

    @DeleteMapping("/subscribe/chatMsg/{index}")
    @Login
    public R consumeChatMsg(@PathVariable Integer index) {
        System.out.println(" =============== ");
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        userSubscriptionService.consumeChatMsg(userId, index);
        return success();
    }

    @DeleteMapping("/subscribe/msg/all")
    @Login
    public R consumeAllMsg() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        userSubscriptionService.consumeAllMsg(userId);
        return success();
    }

    @DeleteMapping("/subscribe/chatMsg/all")
    @Login
    public R consumeAllChatMsg() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        userSubscriptionService.consumeAllChatMsg(userId);
        return success();
    }

    @GetMapping("/is-subscription/{userId}")
    @Login
    public R isSubscription(@PathVariable Long userId) {
        Long currentUserId = JwtUtil.LOGIN_USER_HANDLER.get();
        return success(userSubscriptionService.isSubscription(currentUserId, userId));
    }

    @GetMapping("/subscription")
    @Login
    public R getSubscriptions() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<UserDTO> users = userSubscriptionService.getSubscriptions(userId);
        return success(users);
    }

    @GetMapping("/subscribed")
    @Login
    public R getSubscribedList() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<UserDTO> users = userSubscriptionService.getSubscribedList(userId);
        return success(users);
    }

}
