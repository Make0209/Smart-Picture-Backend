package com.hbpu.smartpicture.config;


import com.hbpu.smartpicture.security.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebMvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(    // 排除特定路径（可选，主要排除 Swagger 等）
                                         "/api/doc.html",
                                         "/api/swagger-ui.html",
                                         "/api/swagger-resources",
                                         "/api/v3/api-docs",
                                         "/api/webjars"
                );
    }
}