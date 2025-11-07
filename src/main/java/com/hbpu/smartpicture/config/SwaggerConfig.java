package com.hbpu.smartpicture.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / Knife4j 配置类 - 支持 Token 认证
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 将安全方案加入 OpenAPI
        return new OpenAPI()
                .info(new Info()
                        .title("智能图库接口文档")
                        .description("接口文档（JWT Token 测试支持）")
                        .version("v1.0"));
    }
}