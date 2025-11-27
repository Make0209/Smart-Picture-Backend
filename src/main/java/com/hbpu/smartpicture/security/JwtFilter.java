package com.hbpu.smartpicture.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbpu.smartpicture.common.BaseResponse;
import com.hbpu.smartpicture.exception.BusinessException;
import com.hbpu.smartpicture.exception.ErrorCode;
import com.hbpu.smartpicture.model.pojo.User;
import com.hbpu.smartpicture.service.impl.UserServiceImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Jwt拦截器
 */
@Slf4j
@Component
@Order(value = 1)
public class JwtFilter implements Filter {

    private final UserServiceImpl userService;

    public JwtFilter(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * 请求白名单
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/doc.html",
            "/api/swagger-ui.html",
            "/api/swagger-resources",
            "/api/v3/api-docs",
            "/api/webjars",
            "/api/health"
    );


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // ⭐⭐⭐ 第一步：处理 OPTIONS 预检请求（必须在最前面！）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("OPTIONS 预检请求: {}", requestURI);
            // 设置 CORS 响应头
            setCorsHeaders(request, response);
            response.setStatus(HttpServletResponse.SC_OK);
            return; // 直接返回，不继续过滤链
        }

        // 第二步：检查白名单
        if (WHITE_LIST.stream().anyMatch(requestURI::startsWith)) {
            log.info("白名单请求: {}", requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 第三步：验证 Token
        String authorization = request.getHeader("Authorization");

        try {
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);

                // 验证 token 是否过期
                JwtUtil.isTokenExpired(token);

                // 从 Redis 中获取当前用户信息
                User currentUser = userService.getCurrentUser(request);

                if (currentUser != null) {
                    log.info("Token 验证通过，用户: {}", currentUser.getUserAccount());
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    log.warn("Redis 中未找到用户信息");
                    handleException(request, response, ErrorCode.NOT_LOGIN_ERROR.getCode(), "未登录");
                }
            } else {
                log.warn("请求头中没有 Token 或格式不正确，URI: {}", requestURI);
                handleException(request, response, ErrorCode.NOT_LOGIN_ERROR.getCode(), "请求头中没有 Token");
            }
        } catch (BusinessException e) {
            // 捕获业务异常并返回 JSON 响应
            log.error("JwtFilter BusinessException: {}", e.getMessage(), e);
            handleException(request, response, e.getCode(), e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常
            log.error("JwtFilter Exception: ", e);
            handleException(request, response, ErrorCode.SYSTEM_ERROR.getCode(), "认证失败");
        }

    }

    /**
     * 设置 CORS 响应头
     */
    private void setCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        // 如果有 Origin 请求头，就设置允许该源
        response.setHeader("Access-Control-Allow-Origin", Objects.requireNonNullElse(origin, "*"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * 统一处理异常响应
     */
    private void handleException(HttpServletRequest request, HttpServletResponse response, int code, String message) throws IOException {
        // 先加 CORS 头
        setCorsHeaders(request, response);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        BaseResponse<?> result = new BaseResponse<>(code, message, null);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(result);

        response.getWriter().write(jsonResponse);
    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}