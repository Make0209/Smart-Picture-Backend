package com.hbpu.smartpicture.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC JSON 配置类
 */
@JsonComponent
public class JsonConfig {

    /**
     * 配置 Jackson ObjectMapper，将 Long 类型转换为字符串以避免精度丢失
     *
     * @param builder Jackson2ObjectMapperBuilder 对象，用于构建 ObjectMapper
     * @return 配置好的 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance); // 将 Long 类型转换为字符串
        module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 将 long 基本类型转换为字符串
        objectMapper.registerModule(module); // 注册模块到 ObjectMapper
        return objectMapper;
    }
}
