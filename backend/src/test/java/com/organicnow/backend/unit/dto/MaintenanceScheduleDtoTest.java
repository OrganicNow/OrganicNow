package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MaintenanceScheduleDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        return m;
    }

    @Test
    void testGetterSetterAndConstructor() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime next = now.plusMonths(6);

        MaintenanceScheduleDto dto = new MaintenanceScheduleDto(
                1L, 2, 3L, "Group A",
                12, now, next,
                5, "Maintenance", "Check system"
        );

        assertEquals(1L, dto.getId());
        assertEquals(2, dto.getScheduleScope());
        assertEquals(3L, dto.getAssetGroupId());
        assertEquals("Group A", dto.getAssetGroupName());
        assertEquals(12, dto.getCycleMonth());
        assertEquals(now, dto.getLastDoneDate());
        assertEquals(next, dto.getNextDueDate());
        assertEquals(5, dto.getNotifyBeforeDate());
        assertEquals("Maintenance", dto.getScheduleTitle());
        assertEquals("Check system", dto.getScheduleDescription());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.of(2025, 2, 1, 9, 30);

        MaintenanceScheduleDto dto = MaintenanceScheduleDto.builder()
                .id(10L)
                .scheduleScope(1)
                .assetGroupId(20L)
                .assetGroupName("Engine Group")
                .cycleMonth(3)
                .lastDoneDate(now)
                .nextDueDate(now.plusMonths(3))
                .notifyBeforeDate(7)
                .scheduleTitle("Engine Check")
                .scheduleDescription("Routine engine inspection")
                .build();

        assertEquals(10L, dto.getId());
        assertEquals(1, dto.getScheduleScope());
        assertEquals(20L, dto.getAssetGroupId());
        assertEquals("Engine Group", dto.getAssetGroupName());
        assertEquals(3, dto.getCycleMonth());
    }

    @Test
    void testFieldComparisonEqualsManual() {
        LocalDateTime now = LocalDateTime.of(2024, 10, 10, 8, 0);

        MaintenanceScheduleDto d1 = MaintenanceScheduleDto.builder()
                .id(1L).scheduleScope(1).assetGroupId(5L).assetGroupName("A")
                .cycleMonth(6).lastDoneDate(now).nextDueDate(now.plusDays(1))
                .notifyBeforeDate(3).scheduleTitle("Test").scheduleDescription("Desc")
                .build();

        MaintenanceScheduleDto d2 = MaintenanceScheduleDto.builder()
                .id(1L).scheduleScope(1).assetGroupId(5L).assetGroupName("A")
                .cycleMonth(6).lastDoneDate(now).nextDueDate(now.plusDays(1))
                .notifyBeforeDate(3).scheduleTitle("Test").scheduleDescription("Desc")
                .build();

        // เปรียบเทียบทีละ field (ไม่ใช้ equals() เพราะ DTO ไม่มี override)
        assertEquals(d1.getId(), d2.getId());
        assertEquals(d1.getScheduleScope(), d2.getScheduleScope());
        assertEquals(d1.getAssetGroupId(), d2.getAssetGroupId());
        assertEquals(d1.getAssetGroupName(), d2.getAssetGroupName());
        assertEquals(d1.getCycleMonth(), d2.getCycleMonth());
        assertEquals(d1.getLastDoneDate(), d2.getLastDoneDate());
        assertEquals(d1.getNextDueDate(), d2.getNextDueDate());
        assertEquals(d1.getNotifyBeforeDate(), d2.getNotifyBeforeDate());
        assertEquals(d1.getScheduleTitle(), d2.getScheduleTitle());
        assertEquals(d1.getScheduleDescription(), d2.getScheduleDescription());
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        LocalDateTime now = LocalDateTime.of(2025, 3, 15, 14, 0);

        MaintenanceScheduleDto original = MaintenanceScheduleDto.builder()
                .id(100L)
                .scheduleScope(2)
                .assetGroupId(50L)
                .assetGroupName("Cooling System")
                .cycleMonth(12)
                .lastDoneDate(now)
                .nextDueDate(now.plusMonths(12))
                .notifyBeforeDate(14)
                .scheduleTitle("Cooling Check")
                .scheduleDescription("Annual inspection")
                .build();

        String json = mapper().writeValueAsString(original);

        MaintenanceScheduleDto clone =
                mapper().readValue(json, MaintenanceScheduleDto.class);

        // เทียบทีละ field
        assertEquals(original.getId(), clone.getId());
        assertEquals(original.getScheduleScope(), clone.getScheduleScope());
        assertEquals(original.getAssetGroupId(), clone.getAssetGroupId());
        assertEquals(original.getAssetGroupName(), clone.getAssetGroupName());
        assertEquals(original.getCycleMonth(), clone.getCycleMonth());
        assertEquals(original.getLastDoneDate(), clone.getLastDoneDate());
        assertEquals(original.getNextDueDate(), clone.getNextDueDate());
        assertEquals(original.getNotifyBeforeDate(), clone.getNotifyBeforeDate());
        assertEquals(original.getScheduleTitle(), clone.getScheduleTitle());
        assertEquals(original.getScheduleDescription(), clone.getScheduleDescription());
    }
}
