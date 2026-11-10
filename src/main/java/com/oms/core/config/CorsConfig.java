package com.oms.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply CORS rules to all backend API endpoints
                        .allowedOrigins("http://localhost:4200") // Explicitly allow your Angular frontend dev server
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Restrict to required standard HTTP methods
                        .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With") // Allowed headers for incoming requests
                        .exposedHeaders("Authorization") // Expose headers so the frontend can read the JWT token response
                        .allowCredentials(true) // Required to safely process HTTP session cookies or secure tokens
                        .maxAge(3600); // Cache pre-flight response configuration for 1 hour to reduce network overhead
            }
        };
    }
}