//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.NotificationDto;
//import com.organicnow.backend.model.MaintenanceSchedule;
//import com.organicnow.backend.model.Notification;
//import com.organicnow.backend.repository.MaintenanceScheduleRepository;
//import com.organicnow.backend.repository.NotificationRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * ✅ Unit test สำหรับ NotificationServiceImpl
// * - Mock repository ทั้งสองตัว
// * - ทดสอบแต่ละเมธอดใน service ให้ครอบคลุม
// */
//class NotificationServiceTest {
//
//    @Mock
//    private NotificationRepository notificationRepository;
//
//    @Mock
//    private MaintenanceScheduleRepository maintenanceScheduleRepository;
//
//    @InjectMocks
//    private NotificationServiceImpl notificationService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 createNotification()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("createNotification() → should save and return NotificationDto")
//    void testCreateNotification() {
//        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
//                .id(1L)
//                .scheduleTitle("Air Filter")
//                .build();
//
//        Notification saved = Notification.builder()
//                .id(10L)
//                .title("Test")
//                .message("Message")
//                .type("TYPE")
//                .isRead(false)
//                .maintenanceSchedule(schedule)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);
//
//        NotificationDto result = notificationService.createNotification("Test", "Message", "TYPE", schedule);
//
//        assertThat(result.getId()).isEqualTo(10L);
//        assertThat(result.getTitle()).isEqualTo("Test");
//        assertThat(result.getMaintenanceScheduleId()).isEqualTo(1L);
//        verify(notificationRepository, times(1)).save(any(Notification.class));
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 getAllNotifications()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("getAllNotifications() → should return all notifications as DTOs")
//    void testGetAllNotifications() {
//        Notification n = Notification.builder().id(1L).title("T").message("M").createdAt(LocalDateTime.now()).build();
//        when(notificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(n));
//
//        List<NotificationDto> result = notificationService.getAllNotifications();
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getTitle()).isEqualTo("T");
//        verify(notificationRepository).findAllByOrderByCreatedAtDesc();
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 getUnreadNotifications()
//    // ---------------------------------------------------------
//    @Test
//    void testGetUnreadNotifications() {
//        Notification n = Notification.builder().id(1L).title("Unread").isRead(false).createdAt(LocalDateTime.now()).build();
//        when(notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()).thenReturn(List.of(n));
//
//        List<NotificationDto> result = notificationService.getUnreadNotifications();
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getTitle()).isEqualTo("Unread");
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 getUnreadCount()
//    // ---------------------------------------------------------
//    @Test
//    void testGetUnreadCount() {
//        when(notificationRepository.countUnreadNotifications()).thenReturn(5L);
//        Long count = notificationService.getUnreadCount();
//        assertThat(count).isEqualTo(5L);
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 markAsRead()
//    // ---------------------------------------------------------
//    @Test
//    void testMarkAsRead() {
//        Notification n = Notification.builder()
//                .id(1L)
//                .title("Old")
//                .isRead(false)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
//        when(notificationRepository.save(any(Notification.class))).thenReturn(n);
//
//        NotificationDto dto = notificationService.markAsRead(1L);
//
//        assertThat(dto.getIsRead()).isTrue();
//        verify(notificationRepository).save(any(Notification.class));
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 markAllAsRead()
//    // ---------------------------------------------------------
//    @Test
//    void testMarkAllAsRead() {
//        Notification n1 = Notification.builder().id(1L).isRead(false).build();
//        Notification n2 = Notification.builder().id(2L).isRead(false).build();
//
//        when(notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()).thenReturn(List.of(n1, n2));
//
//        notificationService.markAllAsRead();
//
//        verify(notificationRepository).saveAll(anyList());
//        assertThat(n1.getIsRead()).isTrue();
//        assertThat(n2.getIsRead()).isTrue();
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 deleteNotification()
//    // ---------------------------------------------------------
//    @Test
//    void testDeleteNotification() {
//        when(notificationRepository.existsById(10L)).thenReturn(true);
//
//        notificationService.deleteNotification(10L);
//
//        verify(notificationRepository).deleteById(10L);
//    }
//
//    @Test
//    void testDeleteNotification_NotFound() {
//        when(notificationRepository.existsById(99L)).thenReturn(false);
//        assertThatThrownBy(() -> notificationService.deleteNotification(99L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Notification not found");
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 deleteNotificationsByMaintenanceSchedule()
//    // ---------------------------------------------------------
//    @Test
//    void testDeleteNotificationsByMaintenanceSchedule() {
//        Notification n = Notification.builder().id(1L).maintenanceSchedule(null).build();
//        when(notificationRepository.findByMaintenanceScheduleId(1L)).thenReturn(List.of(n));
//
//        notificationService.deleteNotificationsByMaintenanceSchedule(1L);
//
//        verify(notificationRepository).deleteAll(anyList());
//    }
//
//    @Test
//    void testDeleteNotificationsByMaintenanceSchedule_NoData() {
//        when(notificationRepository.findByMaintenanceScheduleId(1L)).thenReturn(List.of());
//        notificationService.deleteNotificationsByMaintenanceSchedule(1L);
//        verify(notificationRepository, never()).deleteAll(anyList());
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 createMaintenanceScheduleNotification()
//    // ---------------------------------------------------------
//    @Test
//    void testCreateMaintenanceScheduleNotification() {
//        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
//                .id(1L)
//                .scheduleTitle("Pump Check")
//                .nextDueDate(LocalDateTime.now().plusDays(2))
//                .build();
//
//        when(notificationRepository.save(any(Notification.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        notificationService.createMaintenanceScheduleNotification(schedule);
//
//        verify(notificationRepository).save(any(Notification.class));
//    }
//
//    // ---------------------------------------------------------
//    // 🔹 checkAndCreateDueNotifications()
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("checkAndCreateDueNotifications() → should create notification when due date is today or tomorrow")
//    void testCheckAndCreateDueNotifications_CreatesNew() {
//        LocalDateTime now = LocalDateTime.now();
//
//        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
//                .id(1L)
//                .scheduleTitle("Air Filter")
//                .nextDueDate(now.plusDays(1)) // due tomorrow
//                .build();
//
//        when(maintenanceScheduleRepository.findAll()).thenReturn(List.of(schedule));
//        when(notificationRepository.findByMaintenanceScheduleIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
//        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        notificationService.checkAndCreateDueNotifications();
//
//        verify(notificationRepository).save(any(Notification.class));
//    }
//
//    @Test
//    @DisplayName("checkAndCreateDueNotifications() → should skip if already has recent notification")
//    void testCheckAndCreateDueNotifications_SkipExisting() {
//        LocalDateTime now = LocalDateTime.now();
//        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
//                .id(1L)
//                .scheduleTitle("Fan Check")
//                .nextDueDate(now.plusDays(1))
//                .build();
//
//        Notification recent = Notification.builder()
//                .id(10L)
//                .type("MAINTENANCE_DUE")
//                .createdAt(now.minusHours(2)) // within 1 day
//                .build();
//
//        when(maintenanceScheduleRepository.findAll()).thenReturn(List.of(schedule));
//        when(notificationRepository.findByMaintenanceScheduleIdOrderByCreatedAtDesc(1L))
//                .thenReturn(List.of(recent));
//
//        notificationService.checkAndCreateDueNotifications();
//
//        verify(notificationRepository, never()).save(any(Notification.class));
//    }
//
//    @Test
//    @DisplayName("checkAndCreateDueNotifications() → should handle schedule with no due date")
//    void testCheckAndCreateDueNotifications_NoDueDate() {
//        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
//                .id(1L)
//                .scheduleTitle("Valve Check")
//                .nextDueDate(null)
//                .build();
//
//        when(maintenanceScheduleRepository.findAll()).thenReturn(List.of(schedule));
//
//        notificationService.checkAndCreateDueNotifications();
//
//        verify(notificationRepository, never()).save(any(Notification.class));
//    }
//
//    @Test
//    @DisplayName("checkAndCreateDueNotifications() → should create overdue notification if date passed")
//    void testCheckAndCreateDueNotifications_Overdue() {
//        LocalDateTime now = LocalDateTime.now();
//        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
//                .id(1L)
//                .scheduleTitle("Pipe Cleaning")
//                .nextDueDate(now.minusDays(2)) // overdue 2 days
//                .build();
//
//        when(maintenanceScheduleRepository.findAll()).thenReturn(List.of(schedule));
//        when(notificationRepository.findByMaintenanceScheduleIdOrderByCreatedAtDesc(1L))
//                .thenReturn(List.of());
//        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        notificationService.checkAndCreateDueNotifications();
//
//        verify(notificationRepository).save(any(Notification.class));
//    }
//}
