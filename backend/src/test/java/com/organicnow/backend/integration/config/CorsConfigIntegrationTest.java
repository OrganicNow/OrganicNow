package com.organicnow.backend.integration.config;

import com.organicnow.backend.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CorsConfig.class)
@AutoConfigureMockMvc
class CorsConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * ✅ ตรวจว่า Bean corsConfigurationSource ถูกสร้างใน Context
     */
    @Test
    void testCorsConfigurationSourceBeanExists() {
        assertThat(corsConfigurationSource).isNotNull();
    }

    /**
     * ✅ ตรวจว่า allowed origins ถูกตั้งไว้ครบตาม config
     */
    @Test
    void testCorsAllowedOriginsConfigured() {
        assertThat(corsConfigurationSource).isNotNull();

        // ✅ ใช้ Reflection ดึง config ออกมาจาก source โดยตรงแทนการส่ง request = null
        var field = org.springframework.test.util.ReflectionTestUtils
                .getField(corsConfigurationSource, "corsConfigurations");

        assertThat(field).isInstanceOf(java.util.Map.class);
        var map = (java.util.Map<String, org.springframework.web.cors.CorsConfiguration>) field;

        // ✅ ดึง config ตัวแรกออกมาตรวจ
        var config = map.values().stream().findFirst().orElse(null);
        assertThat(config).isNotNull();

        assertThat(config.getAllowedOrigins())
                .containsExactlyInAnyOrder(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:4173",
                        "http://app.localtest.me",
                        "https://transcondylar-noncorporately-christen.ngrok-free.dev/login"
                );
        assertThat(config.getAllowCredentials()).isTrue();
    }


    /**
     * ✅ ทดสอบ preflight CORS request (OPTIONS)
     * ส่ง Origin ที่อนุญาต → ต้องผ่านและได้ Header ถูกต้อง
     */
    @Test
    void testCorsPreflightRequestAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    /**
     * ❌ Origin ไม่อยู่ใน allowed list → ไม่ควรมี header CORS กลับมา
     */
    @Test
    void testCorsPreflightRequestDeniedOrigin() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://malicious-site.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
