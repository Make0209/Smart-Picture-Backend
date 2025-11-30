package com.hbpu.smartpicture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 * 使用 CorsFilter，可以在 Servlet Filter 层处理 CORS
 * 必须设置最高优先级，在 JwtFilter 之前执行
 */
@Configuration
public class CorsConfig {

    /**
     * 跨域配置过滤器
     * 用于解决前后端分离项目中的跨域问题
     * <p>
     * &#064;Order(Ordered.HIGHEST_PRECEDENCE)  确保此过滤器最先执行
     * 避免在其他过滤器（如认证过滤器）处理前就因跨域问题被拦截
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的请求来源
        // "*" 允许所有域名访问（开发环境使用）
        // 生产环境建议改为具体域名，如：config.addAllowedOriginPattern("https://yourdomain.com");
        config.addAllowedOriginPattern("*");

        // 允许所有 HTTP 方法（GET, POST, PUT, DELETE 等）
        config.addAllowedMethod("*");

        // 允许所有请求头
        // 包括自定义的 Authorization、Content-Type 等
        config.addAllowedHeader("*");

        // 预检请求的缓存时间（秒）
        // 浏览器会缓存预检请求结果，减少 OPTIONS 请求次数，提升性能
        config.setMaxAge(3600L);

        // 注册跨域配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // "/**" 表示对所有接口路径生效
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}