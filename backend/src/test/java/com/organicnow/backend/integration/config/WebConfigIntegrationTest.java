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
@Import(WebConfig.class)  // Ensure WebConfig is imported for integration testing
class WebConfigIntegrationTest {

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        // Instantiate WebConfig before each test
        webConfig = new WebConfig();
    }

    @Test
    void testCorsConfigurationIsApplied() {
        // Arrange
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        // Simulating behavior of the corsConfigurer()
        when(registry.addMapping("/**")).thenReturn(registration);

        // Configuring mock behaviors for CORS setup
        when(registration.allowedOrigins(any(String[].class))).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(any(String[].class))).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);

        // Act: Applying the CORS mappings
        webConfig.corsConfigurer().addCorsMappings(registry);

        // Assert: Verifying that the CORS setup is applied correctly
        verify(registry).addMapping("/**");
        verify(registration).allowedOrigins("http://localhost:5173");
        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        verify(registration).allowedHeaders("*");
        verify(registration).allowCredentials(true);
    }

    @Test
    void testResourceHandlersAreConfigured() {
        // Act: Using the bean from the WebConfig
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration handler1 = mock(ResourceHandlerRegistration.class);
        ResourceHandlerRegistration handler2 = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/uploads/maintenance-photos/**")).thenReturn(handler1);
        when(registry.addResourceHandler("/uploads/payment-proofs/**")).thenReturn(handler2);
        when(handler1.addResourceLocations("file:uploads/maintenance-photos/")).thenReturn(handler1);
        when(handler2.addResourceLocations("file:uploads/payment-proofs/")).thenReturn(handler2);

        // Now calling addResourceHandlers from the corsConfigurer method
        configurer.addResourceHandlers(registry);

        // Assert: Verifying that the resource handlers are correctly set
        verify(registry).addResourceHandler("/uploads/maintenance-photos/**");
        verify(registry).addResourceHandler("/uploads/payment-proofs/**");
        verify(handler1).addResourceLocations("file:uploads/maintenance-photos/");
        verify(handler2).addResourceLocations("file:uploads/payment-proofs/");
    }


    @Test
    void testBeanIsNotNull() {
        // Verifying that the WebMvcConfigurer bean is not null
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assert configurer != null : "WebMvcConfigurer bean should not be null";
    }
}
