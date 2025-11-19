package com.organicnow.backend.unit.schedule;

import com.organicnow.backend.schedule.NotificationScheduler;
import com.organicnow.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotificationSchedulerTest {

    private NotificationService notificationService;
    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        scheduler = new NotificationScheduler(notificationService);
    }

    // ✅ Test 1: เรียก checkMaintenanceDueNotifications() ปกติ
    @Test
    void testCheckMaintenanceDueNotifications_Success() {
        scheduler.checkMaintenanceDueNotifications();
        verify(notificationService, times(1)).checkAndCreateDueNotifications();
    }

    // ✅ Test 2: ถ้ามี Exception → ต้องไม่ throw ออกมา
    @Test
    void testCheckMaintenanceDueNotifications_WithException() {
        doThrow(new RuntimeException("test error"))
                .when(notificationService).checkAndCreateDueNotifications();

        // ไม่ต้อง mock log แล้ว แค่ตรวจว่าไม่ throw
        scheduler.checkMaintenanceDueNotifications();

        verify(notificationService, times(1)).checkAndCreateDueNotifications();
    }

    // ✅ Test 3: เรียก checkMaintenanceDueNotificationsFrequent() ปกติ
    @Test
    void testCheckMaintenanceDueNotificationsFrequent_Success() {
        scheduler.checkMaintenanceDueNotificationsFrequent();
        verify(notificationService, times(1)).checkAndCreateDueNotifications();
    }

    // ✅ Test 4: ถ้ามี Exception ใน frequent check → ต้องไม่ throw ออกมา
    @Test
    void testCheckMaintenanceDueNotificationsFrequent_WithException() {
        doThrow(new RuntimeException("frequent error"))
                .when(notificationService).checkAndCreateDueNotifications();

        scheduler.checkMaintenanceDueNotificationsFrequent();

        verify(notificationService, times(1)).checkAndCreateDueNotifications();
    }
}
