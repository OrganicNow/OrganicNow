package com.organicnow.backend.integration.config;

import com.organicnow.backend.config.SessionConfig;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ Integration Test for SessionConfig
 * ทดสอบว่า SessionConfig ตั้งค่า session cookie และ tracking mode ถูกต้อง
 */
@SpringBootTest
@Import(SessionConfig.class)
class SessionConfigIntegrationTest {

    private SessionConfig sessionConfig;
    private ServletContext servletContext;
    private SessionCookieConfig cookieConfig;

    @BeforeEach
    void setUp() {
        sessionConfig = new SessionConfig();
        servletContext = mock(ServletContext.class);
        cookieConfig = mock(SessionCookieConfig.class);

        when(servletContext.getSessionCookieConfig()).thenReturn(cookieConfig);
    }

    @Test
    void testServletContextInitializerConfiguresSessionSettings() throws ServletException {
        // Arrange
        var initializer = sessionConfig.servletContextInitializer();

        // Act
        initializer.onStartup(servletContext);

        // Assert
        verify(servletContext).setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        verify(cookieConfig).setHttpOnly(true);
        verify(cookieConfig).setSecure(false);
        verify(cookieConfig).setMaxAge(24 * 60 * 60);
        verify(cookieConfig).setPath("/");
        verify(cookieConfig).setName("JSESSIONID");
    }

    @Test
    void testServletContextInitializerIsNotNull() {
        assertNotNull(sessionConfig.servletContextInitializer(), "ServletContextInitializer bean should not be null");
    }

    @Test
    void testSessionTrackingModeIsCookieOnly() throws ServletException {
        var initializer = sessionConfig.servletContextInitializer();
        initializer.onStartup(servletContext);

        // Verify only COOKIE mode is used
        verify(servletContext).setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
    }
}
