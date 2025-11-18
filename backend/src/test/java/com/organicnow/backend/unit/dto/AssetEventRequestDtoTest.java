package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.AssetEventRequestDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetEventRequestDtoTest {

    // ============================================================
    // 1) Default constructor + Setter + Getter
    // ============================================================
    @Test
    void testDefaultConstructorAndSetters() {
        AssetEventRequestDto dto = new AssetEventRequestDto();

        dto.setAssetIds(List.of(1L, 2L, 3L));
        dto.setReasonType("damage");
        dto.setNote("Broken screen");

        assertEquals(List.of(1L, 2L, 3L), dto.getAssetIds());
        assertEquals("damage", dto.getReasonType());
        assertEquals("Broken screen", dto.getNote());
    }

    // ============================================================
    // 2) Test empty assetIds
    // ============================================================
    @Test
    void testEmptyAssetIds() {
        AssetEventRequestDto dto = new AssetEventRequestDto();

        dto.setAssetIds(List.of());
        dto.setReasonType("free");
        dto.setNote("");

        assertTrue(dto.getAssetIds().isEmpty());
        assertEquals("free", dto.getReasonType());
        assertEquals("", dto.getNote());
    }

    // ============================================================
    // 3) Test null values
    // ============================================================
    @Test
    void testNullValues() {
        AssetEventRequestDto dto = new AssetEventRequestDto();

        dto.setAssetIds(null);
        dto.setReasonType(null);
        dto.setNote(null);

        assertNull(dto.getAssetIds());
        assertNull(dto.getReasonType());
        assertNull(dto.getNote());
    }

    // ============================================================
    // 4) Test equals() และ hashCode()
    // ============================================================
    @Test
    void testEqualsAndHashCode() {
        AssetEventRequestDto a = new AssetEventRequestDto();
        a.setAssetIds(List.of(10L, 20L));
        a.setReasonType("addon");
        a.setNote("Extra table");

        AssetEventRequestDto b = new AssetEventRequestDto();
        b.setAssetIds(List.of(10L, 20L));
        b.setReasonType("addon");
        b.setNote("Extra table");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ============================================================
    // 5) equals() → not equal
    // ============================================================
    @Test
    void testNotEquals() {
        AssetEventRequestDto a = new AssetEventRequestDto();
        a.setAssetIds(List.of(10L));
        a.setReasonType("addon");
        a.setNote("test");

        AssetEventRequestDto b = new AssetEventRequestDto();
        b.setAssetIds(List.of(99L)); // ต่างกัน
        b.setReasonType("addon");
        b.setNote("test");

        assertNotEquals(a, b);
    }

    // ============================================================
    // 6) toString() basic test
    // ============================================================
    @Test
    void testToString() {
        AssetEventRequestDto dto = new AssetEventRequestDto();
        dto.setAssetIds(List.of(1L));
        dto.setReasonType("damage");
        dto.setNote("Cracked");

        String ts = dto.toString();

        assertTrue(ts.contains("assetIds=[1]"));
        assertTrue(ts.contains("reasonType=damage"));
        assertTrue(ts.contains("note=Cracked"));
    }
}
