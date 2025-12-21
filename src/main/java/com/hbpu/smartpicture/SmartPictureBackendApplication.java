package com.hbpu.smartpicture;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
@MapperScan("com.hbpu.smartpicture.mapper")
public class SmartPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPictureBackendApplication.class, args);
    }

}
