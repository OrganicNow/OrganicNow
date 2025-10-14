package com.organicnow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CorsConfig - ตั้งค่าให้ Frontend กับ Backend คุยกันได้
 * 
 * ทำไมต้องมีไฟล์นี้?
 * - เพราะ React dev server (localhost:5173) กับ Spring Boot (localhost:8080) อยู่คนละ port
 * - เวลา Frontend เรียก API จะโดน CORS policy block (browser security)
 * - เลยต้องบอก Spring Boot ว่า "อนุญาตให้ localhost:5173 เรียก API ได้นะ"
 * 
 * ไฟล์นี้ทำอะไร:
 * - อนุญาตให้ React เรียก API ทุกตัวได้ (GET, POST, PUT, DELETE)
 * - รองรับการส่ง cookie/session ข้ามโดเมน
 * - ป้องกัน CORS error ที่จะทำให้หน้าเว็บใช้งานไม่ได้
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 🔧 ตั้งค่า CORS แบบง่ายๆ สำหรับ Spring MVC เหมาะสำหรับ basic setup
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                              // ทุก API path
                .allowedOrigins("http://localhost:5173")        // อนุญาต React dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ทุก method ที่ใช้
                .allowedHeaders("*")                            // ทุก header ที่ส่งมา
                .allowCredentials(true)                         // ส่ง cookie/session ได้
                .maxAge(3600);                                  // cache 1 ชม.
    }

    /**
     * 🔧 ตั้งค่า CORS แบบ Bean สำหรับ Security filter
     * วิธีนี้ใช้เมื่อมี Spring Security หรือต้องการ config ละเอียดกว่า
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));  // React dev server
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // HTTP methods
        configuration.setAllowedHeaders(Arrays.asList("*"));           // ทุก header
        configuration.setAllowCredentials(true);                       // cookie/session support
        configuration.setMaxAge(3600L);                                // cache 1 ชม.

        // ลงทะเบียน config นี้ให้ทุก path
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);        // ทุก API endpoint
        return source;
    }
}