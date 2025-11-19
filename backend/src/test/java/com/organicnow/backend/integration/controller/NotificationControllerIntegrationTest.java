package com.organicnow.backend.integration.controller;

import com.organicnow.backend.BackendApplication;
import com.organicnow.backend.dto.NotificationDueDto;
import com.organicnow.backend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationService notificationService;

    // -------------------------------------------------------
    // 1) GET /api/notifications/due
    // -------------------------------------------------------
    @Test
    void getDue_shouldReturnListFromService() throws Exception {
        // given: service คืน list 2 ตัว
        List<NotificationDueDto> mockList = List.of(
                new NotificationDueDto(),
                new NotificationDueDto()
        );
        when(notificationService.getDueNotifications(null)).thenReturn(mockList);

        // when + then
        mockMvc.perform(get("/api/notifications/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getDue_whenNoNotifications_shouldReturnEmptyArray() throws Exception {
        // given: service คืน list ว่าง
        when(notificationService.getDueNotifications(null)).thenReturn(List.of());

        // when + then
        mockMvc.perform(get("/api/notifications/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------
    // 2) DELETE /api/notifications/schedule/{scheduleId}/due/{dueDate}/skip
    // -------------------------------------------------------
    @Test
    void skip_shouldCallServiceAndReturn204() throws Exception {
        Long scheduleId = 10L;
        LocalDate dueDate = LocalDate.of(2025, 11, 20);

        // ให้ service ไม่ทำอะไร (void method)
        doNothing().when(notificationService)
                .skipScheduleDue(eq(scheduleId), eq(dueDate));

        mockMvc.perform(delete("/api/notifications/schedule/{scheduleId}/due/{dueDate}/skip",
                        scheduleId, dueDate.toString()))
                .andExpect(status().isNoContent());

        // verify ว่า service ถูกเรียกด้วยพารามิเตอร์ที่ถูกต้อง
        verify(notificationService).skipScheduleDue(scheduleId, dueDate);
    }

    /**
     * กรณี date format ผิด:
     * - Spring แปลง path variable -> LocalDate ไม่ได้
     * - โดน GlobalExceptionHandler ของโปรเจกต์คุณจัดการ แล้วตอบ 500 + JSON body
     * ดังนั้นเทสต้อง expect 500 ตาม behavior จริง
     */
    @Test
    void skip_withInvalidDate_shouldReturnServerErrorFromGlobalHandler() throws Exception {
        Long scheduleId = 10L;

        mockMvc.perform(delete("/api/notifications/schedule/{scheduleId}/due/{dueDate}/skip",
                        scheduleId, "invalid-date"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("server_error"));
    }
}
