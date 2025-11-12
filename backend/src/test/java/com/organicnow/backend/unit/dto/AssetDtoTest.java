//package com.organicnow.backend.dto;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class AssetDtoTest {
//
//    @Test
//    void testAssetDtoConstructorWithThreeArgs() {
//        // Arrange
//        Long expectedAssetId = 1L;
//        String expectedAssetName = "Asset A";
//        String expectedAssetType = "Type A";
//
//        // Act
//        AssetDto assetDto = new AssetDto(expectedAssetId, expectedAssetName, expectedAssetType);
//
//        // Assert
//        assertEquals(expectedAssetId, assetDto.getAssetId());
//        assertEquals(expectedAssetName, assetDto.getAssetName());
//        assertEquals(expectedAssetType, assetDto.getAssetType());
//        assertNull(assetDto.getFloor());  // floor is not set, should be null
//        assertNull(assetDto.getRoom());   // room is not set, should be null
//        assertNull(assetDto.getStatus()); // status should be null in this case
//    }
//
//
//
//
//    @Test
//    void testAssetDtoConstructorWithFiveArgs() {
//        // Arrange
//        Long expectedAssetId = 1L;
//        String expectedAssetName = "Asset B";
//        String expectedAssetType = "Type B";
//        Integer expectedFloor = 3;
//        String expectedRoom = "101";
//
//        // Act
//        AssetDto assetDto = new AssetDto(expectedAssetId, expectedAssetName, expectedAssetType, expectedFloor, expectedRoom);
//
//        // Assert
//        assertEquals(expectedAssetId, assetDto.getAssetId());
//        assertEquals(expectedAssetName, assetDto.getAssetName());
//        assertEquals(expectedAssetType, assetDto.getAssetType());
//        assertEquals(expectedFloor, assetDto.getFloor());
//        assertEquals(expectedRoom, assetDto.getRoom());
//        assertEquals("Active", assetDto.getStatus()); // Default status is Active
//    }
//
//    @Test
//    void testSettersAndGetters() {
//        // Arrange
//        AssetDto assetDto = new AssetDto();
//        assetDto.setAssetId(2L);
//        assetDto.setAssetName("Asset C");
//        assetDto.setAssetType("Type C");
//        assetDto.setFloor(5);
//        assetDto.setRoom("201");
//        assetDto.setStatus("Inactive");
//
//        // Act & Assert
//        assertEquals(2L, assetDto.getAssetId());
//        assertEquals("Asset C", assetDto.getAssetName());
//        assertEquals("Type C", assetDto.getAssetType());
//        assertEquals(5, assetDto.getFloor());
//        assertEquals("201", assetDto.getRoom());
//        assertEquals("Inactive", assetDto.getStatus());
//    }
//}
