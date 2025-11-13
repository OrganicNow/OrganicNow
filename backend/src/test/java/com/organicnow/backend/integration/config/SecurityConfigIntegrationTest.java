package com.organicnow.backend.integration.config;

import com.organicnow.backend.config.CorsConfig;
import com.organicnow.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({SecurityConfig.class, CorsConfig.class})
class SecurityConfigIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    /**
     * ✅ DummyController — จำลอง endpoint จริง
     * เพื่อให้ MockMvc ใช้งานได้ครบทุก method ที่ SecurityConfig อนุญาต
     */
    @SpringBootApplication
    static class TestApplication {

        @RestController
        @RequestMapping("/api")
        static class DummyController {

            // ✅ รองรับทั้ง GET และ POST สำหรับ /api/auth/login
            @GetMapping("/auth/login")
            public String loginGet() {
                return "login OK (GET)";
            }

            @PostMapping("/auth/login")
            public String loginPost() {
                return "login OK (POST)";
            }

            @GetMapping("/health")
            public String health() {
                return "health OK";
            }
        }
    }

    @Test
    void testAuthEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testOtherEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testCorsConfigurationApplied() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    // ✅ ทดสอบว่า CSRF ถูกปิดจริง (POST ผ่านได้โดยไม่ต้องมี token)
    @Test
    void testCsrfIsDisabled() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\"}"))
                .andExpect(status().isOk());
    }
}
