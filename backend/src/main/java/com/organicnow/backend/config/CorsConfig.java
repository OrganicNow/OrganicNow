package com.organicnow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ เปิดกว้างทุก origin ก่อน (ทั้ง localhost, ngrok, อื่น ๆ)
        config.setAllowedOriginPatterns(List.of("*"));

        // method / header อะไรก็ได้
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // เพราะ frontend ใช้ credentials: 'include'
        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // ใช้ config นี้กับทุก path
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                // ✅ เพิ่ม origin ของ production เข้าไปด้วย
//                .allowedOrigins(
//                        "http://localhost:5173",        // dev
//                        "http://localhost:3000",        // alternative dev
//                        "http://localhost:4173",        // vite preview
//                        "http://app.localtest.me",       // prod (K8s)
//                        "https://transcondylar-noncorporately-christen.ngrok-free.dev"
//                )
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // ✅ ใส่ทั้ง dev และ prod
//        configuration.setAllowedOrigins(List.of(
//                "http://localhost:5173",
//                "http://localhost:3000",
//                "http://localhost:4173",
//                "http://app.localtest.me",
//                "https://transcondylar-noncorporately-christen.ngrok-free.dev/login"
//        ));
//
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}


//package com.organicnow.backend.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.Arrays;
//
///**
// * CorsConfig - ตั้งค่าให้ Frontend กับ Backend คุยกันได้
// *
// * ทำไมต้องมีไฟล์นี้?
// * - เพราะ React dev server (localhost:5173) กับ Spring Boot (localhost:8080) อยู่คนละ port
// * - เวลา Frontend เรียก API จะโดน CORS policy block (browser security)
// * - เลยต้องบอก Spring Boot ว่า "อนุญาตให้ localhost:5173 เรียก API ได้นะ"
// *
// * ไฟล์นี้ทำอะไร:
// * - อนุญาตให้ React เรียก API ทุกตัวได้ (GET, POST, PUT, DELETE)
// * - รองรับการส่ง cookie/session ข้ามโดเมน
// * - ป้องกัน CORS error ที่จะทำให้หน้าเว็บใช้งานไม่ได้
// */
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    /**
//     * 🔧 ตั้งค่า CORS แบบง่ายๆ สำหรับ Spring MVC เหมาะสำหรับ basic setup
//     */
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")                              // ทุก API path
//                .allowedOrigins("http://localhost:5173")        // อนุญาต React dev server
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ทุก method ที่ใช้
//                .allowedHeaders("*")                            // ทุก header ที่ส่งมา
//                .allowCredentials(true)                         // ส่ง cookie/session ได้
//                .maxAge(3600);                                  // cache 1 ชม.
//    }
//
//    /**
//     * 🔧 ตั้งค่า CORS แบบ Bean สำหรับ Security filter
//     * วิธีนี้ใช้เมื่อมี Spring Security หรือต้องการ config ละเอียดกว่า
//     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));  // React dev server
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // HTTP methods
//        configuration.setAllowedHeaders(Arrays.asList("*"));           // ทุก header
//        configuration.setAllowCredentials(true);                       // cookie/session support
//        configuration.setMaxAge(3600L);                                // cache 1 ชม.
//
//        // ลงทะเบียน config นี้ให้ทุก path
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);        // ทุก API endpoint
//        return source;
//    }
//}