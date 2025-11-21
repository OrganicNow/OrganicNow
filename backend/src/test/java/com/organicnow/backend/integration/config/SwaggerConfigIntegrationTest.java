//package com.organicnow.backend.integration.config;
//
//import com.organicnow.backend.config.SwaggerConfig;
//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.info.Info;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * ✅ Integration Test for SwaggerConfig
// * ตรวจสอบว่า Spring โหลด Bean ได้ และ Annotation ของ OpenAPI ถูกต้อง
// */
//@SpringBootTest
//@Import(SwaggerConfig.class)
//class SwaggerConfigIntegrationTest {
//
//    @Test
//    void testSwaggerConfigAnnotationPresent() {
//        // ตรวจสอบว่ามี annotation @OpenAPIDefinition อยู่จริง
//        OpenAPIDefinition openAPIDef = SwaggerConfig.class.getAnnotation(OpenAPIDefinition.class);
//        assertNotNull(openAPIDef, "@OpenAPIDefinition should be present on SwaggerConfig");
//
//        // ตรวจสอบ title, version, description จาก @Info
//        Info info = openAPIDef.info();
//        assertEquals("OrganicNow Backend API", info.title());
//        assertEquals("1.0.0", info.version());
//        assertTrue(info.description().contains("OrganicNow"), "Description should contain project name");
//
//        // ตรวจสอบ contact
//        assertEquals("Dev Team", info.contact().name());
//        assertEquals("support@organicnow.com", info.contact().email());
//
//        // ตรวจสอบ license
//        assertEquals("Apache 2.0", info.license().name());
//        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", info.license().url());
//    }
//
//    @Test
//    void testSwaggerConfigBeanLoadsInSpringContext() {
//        // ทดสอบว่า config โหลดใน Spring context ได้โดยไม่มี error
//        assertDoesNotThrow(() -> new SwaggerConfig(), "SwaggerConfig should be instantiated without error");
//    }
//}
