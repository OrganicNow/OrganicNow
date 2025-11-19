package com.organicnow.backend.integration.config;

import com.organicnow.backend.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ✅ Integration Test for WebConfig
 * ทดสอบการตั้งค่า CORS และ ResourceHandler ที่กำหนดไว้ใน WebMvcConfigurer
 */
@SpringBootTest
@Import(WebConfig.class)
class WebConfigIntegrationTest {

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
    }

    @Test
    void testCorsConfigurationIsApplied() {
        // Arrange
        var corsConfigurer = webConfig.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping("/**")).thenReturn(registration);

        // ✅ ต้องใช้ any(String[].class) เพราะ parameter เป็น varargs
        when(registration.allowedOrigins(any(String[].class))).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(any(String[].class))).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registry).addMapping("/**");
        verify(registration).allowedOrigins("http://localhost:5173");
        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        verify(registration).allowedHeaders("*");
        verify(registration).allowCredentials(true);
    }

    @Test
    void testResourceHandlersAreConfigured() {
        var corsConfigurer = webConfig.corsConfigurer();
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration handler1 = mock(ResourceHandlerRegistration.class);
        ResourceHandlerRegistration handler2 = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/uploads/maintenance-photos/**")).thenReturn(handler1);
        when(registry.addResourceHandler("/uploads/payment-proofs/**")).thenReturn(handler2);
        when(handler1.addResourceLocations("file:uploads/maintenance-photos/")).thenReturn(handler1);
        when(handler2.addResourceLocations("file:uploads/payment-proofs/")).thenReturn(handler2);

        corsConfigurer.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/maintenance-photos/**");
        verify(registry).addResourceHandler("/uploads/payment-proofs/**");
        verify(handler1).addResourceLocations("file:uploads/maintenance-photos/");
        verify(handler2).addResourceLocations("file:uploads/payment-proofs/");
    }

    @Test
    void testBeanIsNotNull() {
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assert configurer != null : "WebMvcConfigurer bean should not be null";
    }
}
