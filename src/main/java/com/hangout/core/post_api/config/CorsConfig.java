package com.hangout.core.post_api.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Value("${hangout.allowed-origins.url}")
    private String clientOrigins;

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/v1/**")
                        .allowedOriginPatterns(getAllowedOrigins(clientOrigins))
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .allowedMethods(HttpMethod.OPTIONS.name(), HttpMethod.POST.name(), HttpMethod.GET.name(),
                                HttpMethod.DELETE.name());
            }
        };
    }

    private String[] getAllowedOrigins(String allowedOrigins) {
        return Arrays.stream(allowedOrigins.split(",")).map(String::trim)
                .toArray(String[]::new);

    }
}
