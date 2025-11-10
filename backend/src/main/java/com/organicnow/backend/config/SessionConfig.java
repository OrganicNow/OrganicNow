package com.organicnow.backend.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.SessionTrackingMode;
import java.util.EnumSet;

@Configuration
public class SessionConfig {

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
                servletContext.getSessionCookieConfig().setHttpOnly(true);
                servletContext.getSessionCookieConfig().setSecure(false); // true for HTTPS
                servletContext.getSessionCookieConfig().setMaxAge(24 * 60 * 60); // 24 hours
                servletContext.getSessionCookieConfig().setPath("/");
                servletContext.getSessionCookieConfig().setName("JSESSIONID");
            }
        };
    }
}