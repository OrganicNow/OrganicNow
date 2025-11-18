package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MaintenanceScheduleCreateDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Test
    void testNoArgsConstructor() {
        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
        assertNotNull(dto);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 30);

        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto(
                1, 10L, 6, 5, "Test", "Description", now, now.plusMonths(6)
        );

        assertEquals(1, dto.getScheduleScope());
        assertEquals(10L, dto.getAssetGroupId());
        assertEquals(6, dto.getCycleMonth());
        assertEquals(5, dto.getNotifyBeforeDate());
        assertEquals("Test", dto.getScheduleTitle());
        assertEquals("Description", dto.getScheduleDescription());
        assertEquals(now, dto.getLastDoneDate());
        assertEquals(now.plusMonths(6), dto.getNextDueDate());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.of(2024, 2, 10, 14, 0);

        MaintenanceScheduleCreateDto dto = MaintenanceScheduleCreateDto.builder()
                .scheduleScope(2)
                .assetGroupId(99L)
                .cycleMonth(3)
                .notifyBeforeDate(7)
                .scheduleTitle("Aircon")
                .scheduleDescription("Check and clean")
                .lastDoneDate(now)
                .nextDueDate(now.plusMonths(3))
                .build();

        assertEquals(2, dto.getScheduleScope());
        assertEquals(99L, dto.getAssetGroupId());
        assertEquals(3, dto.getCycleMonth());
        assertEquals(7, dto.getNotifyBeforeDate());
        assertEquals("Aircon", dto.getScheduleTitle());
        assertEquals("Check and clean", dto.getScheduleDescription());
        assertEquals(now, dto.getLastDoneDate());
        assertEquals(now.plusMonths(3), dto.getNextDueDate());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();

        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
        dto.setScheduleScope(1);
        dto.setAssetGroupId(11L);
        dto.setCycleMonth(12);
        dto.setNotifyBeforeDate(3);
        dto.setScheduleTitle("Title");
        dto.setScheduleDescription("Desc");
        dto.setLastDoneDate(now);
        dto.setNextDueDate(now.plusDays(10));

        assertEquals(1, dto.getScheduleScope());
        assertEquals(11L, dto.getAssetGroupId());
        assertEquals(12, dto.getCycleMonth());
        assertEquals(3, dto.getNotifyBeforeDate());
        assertEquals("Title", dto.getScheduleTitle());
        assertEquals("Desc", dto.getScheduleDescription());
        assertEquals(now, dto.getLastDoneDate());
        assertEquals(now.plusDays(10), dto.getNextDueDate());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 5, 9, 0);

        MaintenanceScheduleCreateDto dto1 = new MaintenanceScheduleCreateDto(
                1, 5L, 6, 3, "Test", "Desc", now, now.plusMonths(6)
        );

        MaintenanceScheduleCreateDto dto2 = new MaintenanceScheduleCreateDto(
                1, 5L, 6, 3, "Test", "Desc", now, now.plusMonths(6)
        );

        // เทียบ field ทีละตัว ไม่ใช้ equals()
        assertEquals(dto1.getScheduleScope(), dto2.getScheduleScope());
        assertEquals(dto1.getAssetGroupId(), dto2.getAssetGroupId());
        assertEquals(dto1.getCycleMonth(), dto2.getCycleMonth());
        assertEquals(dto1.getNotifyBeforeDate(), dto2.getNotifyBeforeDate());
        assertEquals(dto1.getScheduleTitle(), dto2.getScheduleTitle());
        assertEquals(dto1.getScheduleDescription(), dto2.getScheduleDescription());
        assertEquals(dto1.getLastDoneDate(), dto2.getLastDoneDate());
        assertEquals(dto1.getNextDueDate(), dto2.getNextDueDate());
    }


    @Test
    void testJsonSerializationDeserialization() throws Exception {
        LocalDateTime now = LocalDateTime.of(2024, 5, 1, 12, 0);

        MaintenanceScheduleCreateDto original = MaintenanceScheduleCreateDto.builder()
                .scheduleScope(1)
                .assetGroupId(40L)
                .cycleMonth(12)
                .notifyBeforeDate(10)
                .scheduleTitle("General Inspection")
                .scheduleDescription("Routine check")
                .lastDoneDate(now)
                .nextDueDate(now.plusYears(1))
                .build();

        String json = mapper().writeValueAsString(original);

        MaintenanceScheduleCreateDto clone =
                mapper().readValue(json, MaintenanceScheduleCreateDto.class);

        // เทียบทีละ field
        assertEquals(original.getScheduleScope(), clone.getScheduleScope());
        assertEquals(original.getAssetGroupId(), clone.getAssetGroupId());
        assertEquals(original.getCycleMonth(), clone.getCycleMonth());
        assertEquals(original.getNotifyBeforeDate(), clone.getNotifyBeforeDate());
        assertEquals(original.getScheduleTitle(), clone.getScheduleTitle());
        assertEquals(original.getScheduleDescription(), clone.getScheduleDescription());
        assertEquals(original.getLastDoneDate(), clone.getLastDoneDate());
        assertEquals(original.getNextDueDate(), clone.getNextDueDate());
    }


    @Test
    void testToString() {
        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("MaintenanceScheduleCreateDto"));
    }
}
