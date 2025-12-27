package com.hsx.manyue.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.enums.ReturnCodeEnums;
import com.hsx.manyue.common.exception.ApiException;
import com.hsx.manyue.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录鉴权切面
 *
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final HttpServletRequest request;

    @Pointcut("@annotation(com.hsx.manyue.common.annotation.aspect.Login)")
    public void check() {
    }

    @Before("check() && @annotation(login)")
    public void before(JoinPoint joinPoint, Login login) {
        String accessToken = request.getHeader("accessToken");
        if (StrUtil.isBlank(accessToken)) {
            throw new ApiException(ReturnCodeEnums.NO_LOGIN);
        }
        JwtUtil.validateExpire(accessToken);
    }

}
