package com.hbpu.smartpicture.security;

import com.hbpu.smartpicture.annotation.AuthCheck;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求拦截器，对所有需要登录的请求进行拦截并验证token
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 如果不是映射到方法（可能是静态资源等），直接通过
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 2. 获取方法上的注解
        AuthCheck authCheck = handlerMethod.getMethodAnnotation(AuthCheck.class);
        // 3. 核心策略：
        // 如果方法上没有 @AuthCheck 注解，说明不需要鉴权，直接放行！
        // (这样你就不需要维护白名单了，不加注解就是公共接口)
        if (authCheck == null) {
            return true;
        }
        // 4. 如果有注解，说明需要登录，开始校验 Token
        String authorization = request.getHeader("Authorization");
        // 这里可以直接抛出异常，配合你的全局异常处理器 (GlobalExceptionHandler) 返回 JSON
        // 不需要像 Filter 那样手动 response.write
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未携带 Token");
        }
        String token = authorization.substring(7);
        try {
            JwtUtil.isTokenExpired(token); // 校验过期
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 无效或已过期");
        }
        return true; // 校验通过，放行
    }
}