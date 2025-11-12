//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.ApiResponse;
//import com.organicnow.backend.dto.NotificationDto;
//import com.organicnow.backend.service.NotificationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
///**
// * ✅ Unit test สำหรับ NotificationController
// * - ไม่ใช้ Spring context (pure unit test)
// * - ทดสอบเฉพาะ logic ของ controller
// * - ปรับให้ตรงกับ ApiResponse ที่มีฟิลด์ status/result
// */
//class NotificationControllerTest {
//
//    @Mock
//    private NotificationService notificationService;
//
//    @InjectMocks
//    private NotificationController notificationController;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 1. getAllNotifications()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("getAllNotifications() → success")
//    void testGetAllNotifications_Success() {
//        List<NotificationDto> mockList = List.of(
//                NotificationDto.builder()
//                        .id(1L)
//                        .title("Maintenance Reminder")
//                        .message("Check air conditioner")
//                        .type("MAINTENANCE")
//                        .isRead(false)
//                        .createdAt(LocalDateTime.now())
//                        .build()
//        );
//
//        when(notificationService.getAllNotifications()).thenReturn(mockList);
//
//        ResponseEntity<?> response = notificationController.getAllNotifications();
//
//        verify(notificationService).getAllNotifications();
//        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
//
//        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
//        assertThat(body.getStatus()).isEqualTo("success");
//        assertThat(((List<?>) body.getResult())).hasSize(1);
//    }
//
//    @Test
//    @DisplayName("getAllNotifications() → error")
//    void testGetAllNotifications_Failure() {
//        when(notificationService.getAllNotifications()).thenThrow(new RuntimeException("DB error"));
//
//        ResponseEntity<?> response = notificationController.getAllNotifications();
//
//        verify(notificationService).getAllNotifications();
//        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
//
//        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
//        assertThat(body.getStatus()).isEqualTo("error");
//        assertThat(body.getResult()).asString().contains("DB error");
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 2. getUnreadNotifications()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("getUnreadNotifications() → success")
//    void testGetUnreadNotifications_Success() {
//        when(notificationService.getUnreadNotifications()).thenReturn(List.of());
//        ResponseEntity<?> resp = notificationController.getUnreadNotifications();
//
//        verify(notificationService).getUnreadNotifications();
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//
//        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
//        assertThat(body.getStatus()).isEqualTo("success");
//    }
//
//    @Test
//    @DisplayName("getUnreadNotifications() → error")
//    void testGetUnreadNotifications_Failure() {
//        when(notificationService.getUnreadNotifications()).thenThrow(new RuntimeException("Fail"));
//        ResponseEntity<?> resp = notificationController.getUnreadNotifications();
//
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
//        assertThat(body.getStatus()).isEqualTo("error");
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 3. getUnreadCount()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("getUnreadCount() → success")
//    void testGetUnreadCount_Success() {
//        when(notificationService.getUnreadCount()).thenReturn(3L);
//        ResponseEntity<?> resp = notificationController.getUnreadCount();
//
//        verify(notificationService).getUnreadCount();
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//
//        assertThat(body.getStatus()).isEqualTo("success");
//        assertThat(body.getResult()).isEqualTo(3L);
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 4. markAsRead()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("markAsRead() → success")
//    void testMarkAsRead_Success() {
//        NotificationDto dto = NotificationDto.builder()
//                .id(1L)
//                .isRead(true)
//                .build();
//
//        when(notificationService.markAsRead(1L)).thenReturn(dto);
//
//        ResponseEntity<?> resp = notificationController.markAsRead(1L);
//        verify(notificationService).markAsRead(1L);
//
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//        NotificationDto result = (NotificationDto) body.getResult();
//
//        assertThat(body.getStatus()).isEqualTo("success");
//        assertThat(result.getIsRead()).isTrue();
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 5. checkDueNotifications()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("checkDueNotifications() → success")
//    void testCheckDueNotifications_Success() {
//        ResponseEntity<?> resp = notificationController.checkDueNotifications();
//        verify(notificationService).checkAndCreateDueNotifications();
//
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//        assertThat(body.getStatus()).isEqualTo("success");
//        assertThat(body.getResult()).isEqualTo("Due notifications check completed");
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 6. markAllAsRead()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("markAllAsRead() → success")
//    void testMarkAllAsRead_Success() {
//        ResponseEntity<?> resp = notificationController.markAllAsRead();
//        verify(notificationService).markAllAsRead();
//
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//        assertThat(body.getStatus()).isEqualTo("success");
//        assertThat(body.getResult()).isEqualTo("All notifications marked as read");
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 7. deleteNotification()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("deleteNotification() → success")
//    void testDeleteNotification_Success() {
//        ResponseEntity<?> resp = notificationController.deleteNotification(5L);
//        verify(notificationService).deleteNotification(5L);
//
//        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
//        assertThat(body.getStatus()).isEqualTo("success");
//        assertThat(body.getResult()).isEqualTo("Notification deleted successfully");
//    }
//}
