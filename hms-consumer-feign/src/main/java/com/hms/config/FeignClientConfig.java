package com.hms.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.Logger;
import feign.codec.ErrorDecoder;

@Configuration
@EnableFeignClients
public class FeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Set Feign logger level to FULL for detailed logging
    }

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }
}