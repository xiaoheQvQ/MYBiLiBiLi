package com.hsx.manyue.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.hsx.manyue.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录拦截器，获取用户ID
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessToken = request.getHeader("accessToken");
        if (StrUtil.isEmpty(accessToken)) {
            return true;
        }
        try {
            JwtUtil.validateExpire(accessToken);
            Long id = JwtUtil.getIdOfPayload(accessToken);
            JwtUtil.LOGIN_USER_HANDLER.set(id);
        } catch (RuntimeException e) {
            return true;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        JwtUtil.LOGIN_USER_HANDLER.remove();
    }
}
