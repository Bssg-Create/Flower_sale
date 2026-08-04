package com.flower;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
@ComponentScan(basePackages = "com.flower")
@MapperScan(basePackages = "com.flower.mapper")
public class FlowerApplication {
    public static void main(String[] args) {
        validateRequiredEnvironment();
        SpringApplication.run(FlowerApplication.class, args);
    }

    private static void validateRequiredEnvironment() {
        String jwtSecret = requireEnvironmentVariable("FLOWER_JWT_SECRET");
        requireEnvironmentVariable("SPRING_DATASOURCE_USERNAME");
        requireEnvironmentVariable("SPRING_DATASOURCE_PASSWORD");
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("环境变量 FLOWER_JWT_SECRET 必须至少为 32 字节");
        }
    }

    private static String requireEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("缺少必需环境变量: " + name);
        }
        return value;
    }
}
