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

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 开发环境可以用 *，生产环境建议指定域名
        config.addAllowedOriginPattern("*");

        config.setAllowCredentials(true);

        // 可以简化为一行
        config.addAllowedMethod("*");  // 等同于你写的所有方法
        config.addAllowedHeader("*");
        config.addExposedHeader("Authorization");

        // ⭐ 建议开启，可以减少预检请求
        config.setMaxAge(3600L);  // 你注释掉了，建议打开

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}