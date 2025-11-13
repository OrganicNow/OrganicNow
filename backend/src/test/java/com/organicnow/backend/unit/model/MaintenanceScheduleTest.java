package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.MaintenanceSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceScheduleTest {

    @Test
    void testGetterAndSetter() {
        AssetGroup group = new AssetGroup();
        group.setId(1L);
        group.setAssetGroupName("Electrical System");

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setId(10L);
        schedule.setScheduleScope(1);
        schedule.setAssetGroup(group);
        schedule.setCycleMonth(6);
        schedule.setLastDoneDate(LocalDateTime.of(2025, 5, 1, 10, 0));
        schedule.setNextDueDate(LocalDateTime.of(2025, 11, 1, 10, 0));
        schedule.setNotifyBeforeDate(7);
        schedule.setScheduleTitle("Electrical Inspection");
        schedule.setScheduleDescription("Routine 6-month electrical system check");

        assertEquals(10L, schedule.getId());
        assertEquals(1, schedule.getScheduleScope());
        assertEquals(1L, schedule.getAssetGroup().getId());
        assertEquals(6, schedule.getCycleMonth());
        assertEquals(LocalDateTime.of(2025, 5, 1, 10, 0), schedule.getLastDoneDate());
        assertEquals(LocalDateTime.of(2025, 11, 1, 10, 0), schedule.getNextDueDate());
        assertEquals(7, schedule.getNotifyBeforeDate());
        assertEquals("Electrical Inspection", schedule.getScheduleTitle());
        assertEquals("Routine 6-month electrical system check", schedule.getScheduleDescription());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        AssetGroup group = new AssetGroup();
        group.setId(2L);
        group.setAssetGroupName("Water System");

        MaintenanceSchedule schedule = MaintenanceSchedule.builder()
                .id(20L)
                .scheduleScope(0)
                .assetGroup(group)
                .cycleMonth(12)
                .lastDoneDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .nextDueDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .notifyBeforeDate(14)
                .scheduleTitle("Water Pipeline Check")
                .scheduleDescription("Annual maintenance for water system")
                .build();

        assertNotNull(schedule);
        assertEquals(20L, schedule.getId());
        assertEquals(0, schedule.getScheduleScope());
        assertEquals(2L, schedule.getAssetGroup().getId());
        assertEquals(12, schedule.getCycleMonth());
        assertEquals(14, schedule.getNotifyBeforeDate());
        assertEquals("Water Pipeline Check", schedule.getScheduleTitle());
        assertEquals("Annual maintenance for water system", schedule.getScheduleDescription());
    }

    @Test
    void testAllArgsConstructor() {
        AssetGroup group = new AssetGroup();
        group.setId(3L);
        LocalDateTime last = LocalDateTime.of(2025, 3, 1, 10, 0);
        LocalDateTime next = LocalDateTime.of(2025, 9, 1, 10, 0);

        MaintenanceSchedule schedule = new MaintenanceSchedule(
                30L,
                1,
                group,
                3,
                last,
                next,
                10,
                "Safety Check",
                "Fire and smoke alarm system test"
        );

        assertEquals(30L, schedule.getId());
        assertEquals(1, schedule.getScheduleScope());
        assertEquals(3L, schedule.getAssetGroup().getId());
        assertEquals(3, schedule.getCycleMonth());
        assertEquals(last, schedule.getLastDoneDate());
        assertEquals(next, schedule.getNextDueDate());
        assertEquals(10, schedule.getNotifyBeforeDate());
        assertEquals("Safety Check", schedule.getScheduleTitle());
        assertEquals("Fire and smoke alarm system test", schedule.getScheduleDescription());
    }

    @Test
    void testOptionalAssetGroupCanBeNull() {
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setAssetGroup(null);
        assertNull(schedule.getAssetGroup());
    }

    @Test
    void testToStringNotNull() {
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setScheduleTitle("Monthly Air Filter Replacement");
        assertNotNull(schedule.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        MaintenanceSchedule s1 = new MaintenanceSchedule();
        MaintenanceSchedule s3 = new MaintenanceSchedule();

        assertSame(s1, s1);   // อ้างอิงเดียวกัน
        assertNotSame(s1, s3); // คนละอ้างอิง
    }
}
