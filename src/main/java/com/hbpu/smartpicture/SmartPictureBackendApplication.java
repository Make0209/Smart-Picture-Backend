package com.hbpu.smartpicture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class SmartPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPictureBackendApplication.class, args);
    }

}
