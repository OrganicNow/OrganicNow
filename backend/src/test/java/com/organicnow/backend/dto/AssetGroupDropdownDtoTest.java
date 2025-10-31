//package com.organicnow.backend.dto;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class AssetGroupDropdownDtoTest {
//
//    @Test
//    void testNoArgsConstructor() {
//        // Act
//        AssetGroupDropdownDto assetGroupDropdownDto = new AssetGroupDropdownDto();
//
//        // Assert
//        assertNull(assetGroupDropdownDto.getId());  // Default value should be null
//        assertNull(assetGroupDropdownDto.getName()); // Default value should be null
//    }
//
//    @Test
//    void testAllArgsConstructor() {
//        // Arrange
//        Long expectedId = 1L;
//        String expectedName = "Group A";
//
//        // Act
//        AssetGroupDropdownDto assetGroupDropdownDto = new AssetGroupDropdownDto(expectedId, expectedName);
//
//        // Assert
//        assertEquals(expectedId, assetGroupDropdownDto.getId());
//        assertEquals(expectedName, assetGroupDropdownDto.getName());
//    }
//
//    @Test
//    void testBuilder() {
//        // Arrange
//        Long expectedId = 2L;
//        String expectedName = "Group B";
//
//        // Act
//        AssetGroupDropdownDto assetGroupDropdownDto = AssetGroupDropdownDto.builder()
//                .id(expectedId)
//                .name(expectedName)
//                .build();
//
//        // Assert
//        assertEquals(expectedId, assetGroupDropdownDto.getId());
//        assertEquals(expectedName, assetGroupDropdownDto.getName());
//    }
//
//    @Test
//    void testSettersAndGetters() {
//        // Arrange
//        AssetGroupDropdownDto assetGroupDropdownDto = new AssetGroupDropdownDto();
//        Long expectedId = 3L;
//        String expectedName = "Group C";
//
//        // Act
//        assetGroupDropdownDto.setId(expectedId);
//        assetGroupDropdownDto.setName(expectedName);
//
//        // Assert
//        assertEquals(expectedId, assetGroupDropdownDto.getId());
//        assertEquals(expectedName, assetGroupDropdownDto.getName());
//    }
//}
