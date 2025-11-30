package com.hbpu.smartpicture.config;

import com.obs.services.ObsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 华为对象存储配置类
 */
@Configuration
@ConfigurationProperties(prefix = "huawei.obs")
@Data
public class HuaweiObsConfig {
    //访问地址
    private String endPoint;
    //访问key
    private String ak;
    //访问密钥
    private String sk;

    //当结束使用时自动关闭释放资源
    @Bean(destroyMethod = "close")
    public ObsClient obsClient() {
        return new ObsClient(ak, sk, endPoint);
    }
}
