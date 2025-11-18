package com.organicnow.backend.integration.controller;

import com.organicnow.backend.BackendApplication;
import com.organicnow.backend.controller.FileUploadExceptionAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = {
                BackendApplication.class,
                FileUploadExceptionAdviceIntegrationTest.TestUploadController.class
        },
        properties = {
                "spring.servlet.multipart.enabled=true",
                "spring.servlet.multipart.max-file-size=5MB",
                "spring.servlet.multipart.max-request-size=5MB"
        }
)
@AutoConfigureMockMvc
class FileUploadExceptionAdviceIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    // ✅ อัดฉีด ControllerAdvice จริงจาก context
    @Autowired
    FileUploadExceptionAdvice fileUploadExceptionAdvice;

    // Dummy Controller สำหรับเทส flow ปกติ
    @RestController
    static class TestUploadController {
        @PostMapping("/upload-test")
        public String upload(@RequestParam("file") MultipartFile file) {
            return "OK";
        }
    }

    // ------------------------------
    // Helper: ไฟล์ปกติ (ขนาดเล็ก)
    // ------------------------------
    private MockMultipartFile smallFile() {
        return new MockMultipartFile(
                "file",
                "normal.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy".getBytes()
        );
    }

    // ------------------------------
    // ✅ Test 1: เทสตัว Advice โดยตรง
    // ------------------------------
    @Test
    void handleMaxUploadSize_shouldReturn413AndMessage() {
        // เตรียม exception เหมือนตอนอัพไฟล์เกิน 5MB
        MaxUploadSizeExceededException ex =
                new MaxUploadSizeExceededException(5 * 1024 * 1024);

        // เรียกเมธอดใน FileUploadExceptionAdvice ตรง ๆ
        ResponseEntity<String> response =
                fileUploadExceptionAdvice.handleMaxSizeException(ex);

        // ตรวจ status และข้อความ
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody())
                .isEqualTo("ขนาดไฟล์เกินขีดจำกัด! อนุญาตไม่เกิน 5MB");
    }

    // ------------------------------
    // ✅ Test 2: อัพโหลดไฟล์ปกติ → 200 OK
    // ------------------------------
    @Test
    void normalFileUpload_shouldReturn200() throws Exception {
        mockMvc.perform(
                        multipart("/upload-test")
                                .file(smallFile())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    // ------------------------------
    // ✅ Test 3: ไม่ส่งไฟล์มาเลย → 400 Bad Request
    // ------------------------------
    @Test
    void missingFileParameter_shouldReturn400() throws Exception {
        mockMvc.perform(multipart("/upload-test"))
                .andExpect(status().isBadRequest());
    }
}
