package com.hbpu.smartpicture.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;
    private Integer port;
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        //设置redis的地址
        String redisAddress = String.format("redis://%s:%d", host, port);
        // 创建新的配置实例
        Config config = new Config();
        // 设置使用单服务并设置redis地址和数据库
        config.useSingleServer().setAddress(redisAddress).setDatabase(1).setPassword(password);
        // 返回新的RedissonClient对象
        return Redisson.create(config);
    }
}
