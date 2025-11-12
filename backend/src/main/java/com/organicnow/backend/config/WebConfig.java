package com.organicnow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
            
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serve uploaded maintenance photos
                registry.addResourceHandler("/uploads/maintenance-photos/**")
                        .addResourceLocations("file:uploads/maintenance-photos/");
                        
                // Serve payment proof files (existing functionality)  
                registry.addResourceHandler("/uploads/payment-proofs/**")
                        .addResourceLocations("file:uploads/payment-proofs/");
            }
        };
    }
}
