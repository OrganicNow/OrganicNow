package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.NotificationDueDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationDueDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        return m;
    }

    @Test
    void testGetterSetter() {
        NotificationDueDto dto = new NotificationDueDto();

        dto.setScheduleId(10L);
        dto.setScheduleTitle("Engine Maintenance");
        dto.setScheduleDescription("Check engine oil and filter");
        dto.setNextDueDate(LocalDate.of(2025, 12, 10));
        dto.setNotifyDays(7);
        dto.setNotifyAt(Instant.parse("2025-12-03T00:00:00Z"));
        dto.setActionUrl("/maintenance?scheduleId=10");
        dto.setTitle("Maintenance due soon");
        dto.setMessage("กำหนดตรวจ 2025-12-10 (แจ้งล่วงหน้า 7 วัน)");

        assertEquals(10L, dto.getScheduleId());
        assertEquals("Engine Maintenance", dto.getScheduleTitle());
        assertEquals("Check engine oil and filter", dto.getScheduleDescription());
        assertEquals(LocalDate.of(2025, 12, 10), dto.getNextDueDate());
        assertEquals(7, dto.getNotifyDays());
        assertEquals(Instant.parse("2025-12-03T00:00:00Z"), dto.getNotifyAt());
        assertEquals("/maintenance?scheduleId=10", dto.getActionUrl());
        assertEquals("Maintenance due soon", dto.getTitle());
        assertEquals("กำหนดตรวจ 2025-12-10 (แจ้งล่วงหน้า 7 วัน)", dto.getMessage());
    }

    @Test
    void testBuilderStyleManualCreation() {
        NotificationDueDto dto = new NotificationDueDto();
        dto.setScheduleId(1L);
        dto.setScheduleTitle("Test");
        dto.setScheduleDescription("Desc");

        assertEquals(1L, dto.getScheduleId());
        assertEquals("Test", dto.getScheduleTitle());
        assertEquals("Desc", dto.getScheduleDescription());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        NotificationDueDto original = new NotificationDueDto();
        original.setScheduleId(55L);
        original.setScheduleTitle("Water Pump Check");
        original.setScheduleDescription("ตรวจปั๊มน้ำ");
        original.setNextDueDate(LocalDate.of(2025, 5, 20));
        original.setNotifyDays(5);
        original.setNotifyAt(Instant.parse("2025-05-15T00:00:00Z"));
        original.setActionUrl("/maintenance?scheduleId=55");
        original.setTitle("Maintenance Reminder");
        original.setMessage("ถึงกำหนดตรวจสอบปั๊มน้ำแล้ว");

        String json = mapper().writeValueAsString(original);
        NotificationDueDto clone = mapper().readValue(json, NotificationDueDto.class);

        assertEquals(original.getScheduleId(), clone.getScheduleId());
        assertEquals(original.getScheduleTitle(), clone.getScheduleTitle());
        assertEquals(original.getScheduleDescription(), clone.getScheduleDescription());
        assertEquals(original.getNextDueDate(), clone.getNextDueDate());
        assertEquals(original.getNotifyDays(), clone.getNotifyDays());
        assertEquals(original.getNotifyAt(), clone.getNotifyAt());
        assertEquals(original.getActionUrl(), clone.getActionUrl());
        assertEquals(original.getTitle(), clone.getTitle());
        assertEquals(original.getMessage(), clone.getMessage());
    }

    @Test
    void testNullSafety() {
        NotificationDueDto dto = new NotificationDueDto();
        assertNull(dto.getScheduleId());
        assertNull(dto.getScheduleTitle());
        assertNull(dto.getScheduleDescription());
        assertNull(dto.getNextDueDate());
        assertNull(dto.getNotifyDays());
        assertNull(dto.getNotifyAt());
        assertNull(dto.getActionUrl());
        assertNull(dto.getTitle());
        assertNull(dto.getMessage());
    }

    @Test
    void testEqualsFieldByField() {
        NotificationDueDto a = new NotificationDueDto();
        NotificationDueDto b = new NotificationDueDto();

        a.setScheduleId(1L);
        b.setScheduleId(1L);

        a.setScheduleTitle("ABC");
        b.setScheduleTitle("ABC");

        a.setNextDueDate(LocalDate.of(2025, 1, 1));
        b.setNextDueDate(LocalDate.of(2025, 1, 1));

        a.setNotifyAt(Instant.parse("2025-01-01T00:00:00Z"));
        b.setNotifyAt(Instant.parse("2025-01-01T00:00:00Z"));

        assertEquals(a.getScheduleId(), b.getScheduleId());
        assertEquals(a.getScheduleTitle(), b.getScheduleTitle());
        assertEquals(a.getNextDueDate(), b.getNextDueDate());
        assertEquals(a.getNotifyAt(), b.getNotifyAt());
    }
}
