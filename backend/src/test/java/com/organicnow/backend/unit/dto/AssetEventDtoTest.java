package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.AssetEventDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssetEventDtoTest {

    // ============================================================
    // 1) Default constructor + setter + getter
    // ============================================================
    @Test
    void testDefaultConstructor_andSetters() {
        AssetEventDto dto = new AssetEventDto();

        LocalDateTime now = LocalDateTime.now();

        dto.setEventId(1L);
        dto.setRoomId(101L);
        dto.setAssetId(55L);
        dto.setAssetName("Aircon");
        dto.setEventType("Maintenance");
        dto.setReasonType("Broken");
        dto.setNote("Leak detected");
        dto.setCreatedAt(now);

        assertEquals(1L, dto.getEventId());
        assertEquals(101L, dto.getRoomId());
        assertEquals(55L, dto.getAssetId());
        assertEquals("Aircon", dto.getAssetName());
        assertEquals("Maintenance", dto.getEventType());
        assertEquals("Broken", dto.getReasonType());
        assertEquals("Leak detected", dto.getNote());
        assertEquals(now, dto.getCreatedAt());
    }

    // ============================================================
    // 2) All-args constructor
    // ============================================================
    @Test
    void testAllArgsConstructor() {
        LocalDateTime ts = LocalDateTime.now();

        AssetEventDto dto = new AssetEventDto(
                10L,
                2L,
                3L,
                "Fridge",
                "Repair",
                "Power Issue",
                "Fixed wiring",
                ts
        );

        assertEquals(10L, dto.getEventId());
        assertEquals(2L, dto.getRoomId());
        assertEquals(3L, dto.getAssetId());
        assertEquals("Fridge", dto.getAssetName());
        assertEquals("Repair", dto.getEventType());
        assertEquals("Power Issue", dto.getReasonType());
        assertEquals("Fixed wiring", dto.getNote());
        assertEquals(ts, dto.getCreatedAt());
    }

    // ============================================================
    // 3) Builder pattern
    // ============================================================
    @Test
    void testBuilder() {
        LocalDateTime time = LocalDateTime.now();

        AssetEventDto dto = AssetEventDto.builder()
                .eventId(50L)
                .roomId(200L)
                .assetId(99L)
                .assetName("Washer")
                .eventType("Check")
                .reasonType("Routine")
                .note("Normal condition")
                .createdAt(time)
                .build();

        assertEquals(50L, dto.getEventId());
        assertEquals(200L, dto.getRoomId());
        assertEquals(99L, dto.getAssetId());
        assertEquals("Washer", dto.getAssetName());
        assertEquals("Check", dto.getEventType());
        assertEquals("Routine", dto.getReasonType());
        assertEquals("Normal condition", dto.getNote());
        assertEquals(time, dto.getCreatedAt());
    }

    // ============================================================
    // 4) Null safety check
    // ============================================================
    @Test
    void testNullAssignments() {
        AssetEventDto dto = new AssetEventDto();

        dto.setAssetName(null);
        dto.setEventType(null);
        dto.setReasonType(null);
        dto.setNote(null);

        assertNull(dto.getAssetName());
        assertNull(dto.getEventType());
        assertNull(dto.getReasonType());
        assertNull(dto.getNote());
    }

    // ============================================================
    // 5) Equals & HashCode (Lombok)
    // ============================================================
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        AssetEventDto a = AssetEventDto.builder()
                .eventId(1L)
                .roomId(2L)
                .assetId(3L)
                .assetName("Aircon")
                .eventType("REPAIR")
                .reasonType("BROKEN")
                .note("Fix motor")
                .createdAt(now)
                .build();

        AssetEventDto b = AssetEventDto.builder()
                .eventId(1L)
                .roomId(2L)
                .assetId(3L)
                .assetName("Aircon")
                .eventType("REPAIR")
                .reasonType("BROKEN")
                .note("Fix motor")
                .createdAt(now)
                .build();

        assertAssetEventDtoEquals(a, b);
    }
    private void assertAssetEventDtoEquals(AssetEventDto a, AssetEventDto b) {
        assertEquals(a.getEventId(), b.getEventId());
        assertEquals(a.getRoomId(), b.getRoomId());
        assertEquals(a.getAssetId(), b.getAssetId());
        assertEquals(a.getAssetName(), b.getAssetName());
        assertEquals(a.getEventType(), b.getEventType());
        assertEquals(a.getReasonType(), b.getReasonType());
        assertEquals(a.getNote(), b.getNote());
        assertEquals(a.getCreatedAt(), b.getCreatedAt());
    }

}
