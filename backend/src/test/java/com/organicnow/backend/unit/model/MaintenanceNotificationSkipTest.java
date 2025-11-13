package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.MaintenanceNotificationSkip;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceNotificationSkipTest {

    @Test
    void testGetterAndSetter() {
        MaintenanceNotificationSkip skip = new MaintenanceNotificationSkip();
        skip.setSkipId(1L);
        skip.setScheduleId(101L);
        skip.setDueDate(LocalDate.of(2025, 11, 15));

        Instant now = Instant.now();
        skip.setSkippedByUserAt(now);

        assertEquals(1L, skip.getSkipId());
        assertEquals(101L, skip.getScheduleId());
        assertEquals(LocalDate.of(2025, 11, 15), skip.getDueDate());
        assertEquals(now, skip.getSkippedByUserAt());
    }

    @Test
    void testDefaultSkippedByUserAtIsNotNull() {
        MaintenanceNotificationSkip skip = new MaintenanceNotificationSkip();

        assertNotNull(skip.getSkippedByUserAt());
        assertTrue(skip.getSkippedByUserAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testUpdateSkippedByUserAt() throws InterruptedException {
        MaintenanceNotificationSkip skip = new MaintenanceNotificationSkip();
        Instant first = skip.getSkippedByUserAt();

        Thread.sleep(10); // simulate time passing
        Instant newTime = Instant.now();
        skip.setSkippedByUserAt(newTime);

        assertNotEquals(first, skip.getSkippedByUserAt());
        assertEquals(newTime, skip.getSkippedByUserAt());
    }

    @Test
    void testToStringLikeRepresentation() {
        MaintenanceNotificationSkip skip = new MaintenanceNotificationSkip();
        skip.setScheduleId(50L);
        skip.setDueDate(LocalDate.of(2025, 12, 1));

        String result = "skipId=" + skip.getSkipId() +
                ", scheduleId=" + skip.getScheduleId() +
                ", dueDate=" + skip.getDueDate() +
                ", skippedByUserAt=" + skip.getSkippedByUserAt();

        assertTrue(result.contains("scheduleId=50"));
        assertTrue(result.contains("dueDate=2025-12-01"));
    }

    @Test
    void testEqualsReferenceOnly() {
        MaintenanceNotificationSkip s1 = new MaintenanceNotificationSkip();
        MaintenanceNotificationSkip s3 = new MaintenanceNotificationSkip();

        assertSame(s1, s1);   // อ้างอิงเดียวกัน
        assertNotSame(s1, s3); // คนละอ้างอิง
    }
}
