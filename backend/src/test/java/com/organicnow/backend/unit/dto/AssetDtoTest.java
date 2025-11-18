package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.AssetDto;  // ← แก้ตรงนี้ สำคัญมาก

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssetDtoTest {

    @Test
    void testDefaultConstructorAndSetters() {
        AssetDto dto = new AssetDto();

        dto.setAssetId(10L);
        dto.setAssetName("Air Conditioner");
        dto.setAssetGroupName("Electronics");
        dto.setAssetGroupId(5L);
        dto.setFloor(2);
        dto.setRoom("201");
        dto.setStatus("Inactive");

        assertEquals(10L, dto.getAssetId());
        assertEquals("Air Conditioner", dto.getAssetName());
        assertEquals("Electronics", dto.getAssetGroupName());
        assertEquals(5L, dto.getAssetGroupId());
        assertEquals(2, dto.getFloor());
        assertEquals("201", dto.getRoom());
        assertEquals("Inactive", dto.getStatus());
    }

    @Test
    void testConstructor_Minimal() {
        AssetDto dto = new AssetDto(1L, "Desk", "Furniture");

        assertEquals(1L, dto.getAssetId());
        assertEquals("Desk", dto.getAssetName());
        assertEquals("Furniture", dto.getAssetGroupName());
        assertNull(dto.getFloor());
        assertNull(dto.getRoom());
        assertNull(dto.getStatus());
        assertNull(dto.getAssetGroupId());
    }

    @Test
    void testConstructor_WithRoomInfo_DefaultStatusActive() {
        AssetDto dto = new AssetDto(2L, "Bed", "Furniture", 3, "302");

        assertEquals(2L, dto.getAssetId());
        assertEquals("Bed", dto.getAssetName());
        assertEquals("Furniture", dto.getAssetGroupName());
        assertEquals(3, dto.getFloor());
        assertEquals("302", dto.getRoom());
        assertEquals("Active", dto.getStatus());
        assertNull(dto.getAssetGroupId());
    }

    @Test
    void testConstructor_WithRoomInfoAndStatus() {
        AssetDto dto = new AssetDto(3L, "Lamp", "Electronics", 1, "101", "Broken");

        assertEquals(3L, dto.getAssetId());
        assertEquals("Lamp", dto.getAssetName());
        assertEquals("Electronics", dto.getAssetGroupName());
        assertEquals(1, dto.getFloor());
        assertEquals("101", dto.getRoom());
        assertEquals("Broken", dto.getStatus());
        assertNull(dto.getAssetGroupId());
    }

    @Test
    void testFullConstructor() {
        AssetDto dto = new AssetDto(4L, "TV", "Electronics", 88L, 5, "501", "Active");

        assertEquals(4L, dto.getAssetId());
        assertEquals("TV", dto.getAssetName());
        assertEquals("Electronics", dto.getAssetGroupName());
        assertEquals(88L, dto.getAssetGroupId());
        assertEquals(5, dto.getFloor());
        assertEquals("501", dto.getRoom());
        assertEquals("Active", dto.getStatus());
    }

    @Test
    void testNullValues() {
        AssetDto dto = new AssetDto();

        dto.setAssetName(null);
        dto.setRoom(null);
        dto.setStatus(null);

        assertNull(dto.getAssetName());
        assertNull(dto.getRoom());
        assertNull(dto.getStatus());
    }
}
