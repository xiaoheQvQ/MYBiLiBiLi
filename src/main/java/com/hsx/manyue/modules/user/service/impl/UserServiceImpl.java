package com.hsx.manyue.modules.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hsx.manyue.common.config.RsaProperty;
import com.hsx.manyue.common.constant.RedisKeys;
import com.hsx.manyue.common.constant.UserConstant;
import com.hsx.manyue.common.dto.Token;
import com.hsx.manyue.common.enums.ReturnCodeEnums;
import com.hsx.manyue.common.exception.ApiException;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.PageUtils;
import com.hsx.manyue.modules.apply.model.vo.MemberInfoVO;
import com.hsx.manyue.modules.apply.service.MemberService;
import com.hsx.manyue.modules.oss.service.IOssService;
import com.hsx.manyue.modules.user.mapper.UserMapper;
import com.hsx.manyue.modules.user.model.dto.UserDTO;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.user.model.params.UserParam;
import com.hsx.manyue.modules.user.service.IRefreshTokenService;
import com.hsx.manyue.modules.user.service.IUserService;
import com.hsx.manyue.modules.user.service.IUserSubscriptionService;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 用户表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements IUserService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final RsaProperty rsaProperty;
    private final IRefreshTokenService refreshTokenService;
    private final MemberService memberService;


    @Value("${spring.mail.username}")
    private String SERVICE_EMAIL;
    private final IOssService ossService;

    @Resource
    @Lazy
    private IUserSubscriptionService userSubscriptionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signup(UserParam user) {
        user.setEmail(rsaProperty.decryptByPrivateKey(user.getEmail()));
        exitsAccount(user.getEmail());
        /* 注册账号 */
        String salt = RandomUtil.randomString(6);
        UserEntity userEntity = Convert.convert(UserEntity.class, user)
                .setSalt(salt)
                .setAccountType("1")
                .setPassword(SecureUtil.md5(rsaProperty.decryptByPrivateKey(user.getPassword()) + salt))
                .setAvatar(UserConstant.DEFAULT_AVATAR);


        System.out.println(userEntity);


        String captcha = redisTemplate.opsForValue().get(RedisKeys.EMAIL_CAPTCHA + userEntity.getEmail());
        if (!StrUtil.equals(captcha, user.getCaptcha())) {
            throw new ApiException(ReturnCodeEnums.CAPTCHA_ERROR);
        }


        System.out.println(userEntity);


        this.save(userEntity);
        /* 注册账号 */
    }

    /**
     * 检查是否存在该邮箱账号
     *
     * @param email
     * @return
     */
    private void exitsAccount(String email) {
        if (this.lambdaQuery().eq(UserEntity::getEmail, email).exists()) {
            throw new ApiException(ReturnCodeEnums.EXITS_ACCOUNT);
        }
    }

    @Override
    public void sendCaptcha(String email) {
        String captcha = RandomUtil.randomNumbers(6);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        redisTemplate.opsForValue().set(RedisKeys.EMAIL_CAPTCHA + email, captcha, 1, TimeUnit.MINUTES);
        System.out.println(redisTemplate.opsForValue().get(RedisKeys.EMAIL_CAPTCHA + email));
        mailMessage.setFrom(SERVICE_EMAIL);
        mailMessage.setTo(email);
        mailMessage.setText("欢迎使用小何的漫跃视频推荐系统，您的验证码是："+captcha+"请在 1 分钟内使用验证码" );
        mailMessage.setSubject("验证码");
        mailSender.send(mailMessage);
    }

    @Override
    public Token login(UserParam user) {
        String email = rsaProperty.decryptByPrivateKey(user.getEmail());
        String password = rsaProperty.decryptByPrivateKey(user.getPassword());
        System.out.println("email:" + email);
        System.out.println("password:" + password);
        UserEntity userEntity = getUserByEmail(email);
        String salt = userEntity.getSalt();
        if (!SecureUtil.md5(password + salt).equals(userEntity.getPassword())) {
            throw new ApiException(ReturnCodeEnums.PASSWORD_ERROR);
        }

        if (StrUtil.equals("0", user.getType())) {
            if (!StrUtil.equals(userEntity.getAccountType(), user.getType())) {
                throw new ApiException(ReturnCodeEnums.LOGIN_TYPE_ERROR);
            }
        }

        Token token = this.generateToken(userEntity.getId());
        System.out.println(token);
        refreshTokenService.setToken(userEntity.getId(), token.getRefreshToken());
        return token;
    }

    /**
     * 生成 token 实体
     *
     * @param id
     * @return
     */
    @Override
    public Token generateToken(Long id) {
        String accessToken = JwtUtil.generateAccessToken(id);
        String refreshToken = JwtUtil.generateRefreshToken(id);
        return new Token(accessToken, refreshToken);
    }

    @Override
    public Token refreshToken(String refreshToken) {

        try {
            JwtUtil.validateExpire(refreshToken);

            Long id = JwtUtil.getIdOfPayload(refreshToken);
            if (!refreshTokenService.exitsRefreshToken(id, refreshToken)) {
                throw new ApiException(ReturnCodeEnums.REFRESH_TOKEN_INVALID);
            }
            Token token = generateToken(id);
            refreshTokenService.setToken(id, token.getRefreshToken());
            return token;
        } catch (ApiException e) {
            throw new ApiException(ReturnCodeEnums.REFRESH_TOKEN_INVALID);
        }
    }


    @Override
    public UserDTO getInfoById(Long userId) {
        // 获取粉丝数量和关注数量
        Long followerCount = userSubscriptionService.countFollowers(userId);
        Long followingCount = userSubscriptionService.countFollowing(userId);
        MemberInfoVO memberInfoVO = memberService.getMemberInfo(userId);
        UserDTO bean = BeanUtil.toBean(this.getById(userId), UserDTO.class);
        bean.setFollowerCount(followerCount);
        bean.setFollowingCount(followingCount);
        bean.setMemberInfoVO(memberInfoVO);
        return bean;
    }

    @Override
    public void updateInfo(UserParam param) {
        String key = RedisKeys.AVATAR + param.getId();
        Boolean cantUploadAvatar = redisTemplate.hasKey(key);
        MultipartFile avatarFile = param.getAvatarFile();
        if (cantUploadAvatar && ObjectUtil.isNotNull(avatarFile)) {
            throw new ApiException(ReturnCodeEnums.CANT_UPLOAD_AVATAR_TODAY);
        }

        UserEntity entity = BeanUtil.toBean(param, UserEntity.class);
        if (ObjectUtil.isNotNull(avatarFile)) {
            Assert.isTrue(avatarFile.getSize() <= 1024 * 1024 * 10, "上传的头像不能超过 10 M");
            String url = ossService.uploadFile(avatarFile);
            redisTemplate.opsForValue().set(key, "", 1, TimeUnit.DAYS);
            entity.setAvatar(url);
            System.out.println("URL："+url);
        }

        this.updateById(entity);
    }

    @Override
    public IPage<UserDTO> getPage(VideoQueryParam param) {
        // 实现根据搜索条件模糊查询用户信息
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getNick, param.getKeyword());
        // 转换类
        IPage<UserEntity> pageData = page(new Page<>(param.getCurrent(), param.getSize()), queryWrapper);
        // 返回分页
        return PageUtils.convert(pageData, UserDTO.class);
    }

    /**
     * 通过邮箱获取用户信息
     *
     * @param email
     * @return
     */
    private UserEntity getUserByEmail(String email) {
        UserEntity user = this.lambdaQuery().eq(UserEntity::getEmail, email)
                .one();

        if (user == null) {
            throw new ApiException(ReturnCodeEnums.NO_USER);
        }
        return user;
    }
}
