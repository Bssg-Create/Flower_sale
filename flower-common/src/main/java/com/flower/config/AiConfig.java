package com.flower.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class AiConfig {
    private String apiKey;
    private String baseUrl;
    private String model = "deepseek-chat";
}