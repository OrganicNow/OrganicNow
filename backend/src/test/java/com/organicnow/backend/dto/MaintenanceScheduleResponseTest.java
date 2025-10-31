//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MaintenanceScheduleResponseTest {
//
//    @Test
//    void testMaintenanceScheduleResponseConstructorAndGettersSetters() {
//        // Arrange
//        LocalDateTime now = LocalDateTime.now(); // ใช้เวลาปัจจุบันแทนการใช้ null
//
//        // สร้าง MaintenanceScheduleDto
//        MaintenanceScheduleDto schedule1 = new MaintenanceScheduleDto(
//                1L, 0, 101L, "A305", 6, now, now, 5, "Schedule 1", "Description 1"
//        );
//
//        MaintenanceScheduleDto schedule2 = new MaintenanceScheduleDto(
//                2L, 1, 102L, "B305", 6, now, now, 5, "Schedule 2", "Description 2"
//        );
//
//        // สร้าง AssetGroupDropdownDto
//        AssetGroupDropdownDto assetGroup1 = new AssetGroupDropdownDto(1L, "Asset Group 1");
//        AssetGroupDropdownDto assetGroup2 = new AssetGroupDropdownDto(2L, "Asset Group 2");
//
//        // สร้าง MaintenanceScheduleResponse
//        MaintenanceScheduleResponse response = MaintenanceScheduleResponse.builder()
//                .result(Arrays.asList(schedule1, schedule2)) // List of schedules
//                .assetGroupDropdown(Arrays.asList(assetGroup1, assetGroup2)) // List of asset groups
//                .build();
//
//        // Act and Assert: ตรวจสอบว่าค่าตั้งถูกต้อง
//        assertNotNull(response.getResult(), "Result should not be null");
//        assertEquals(2, response.getResult().size(), "There should be 2 maintenance schedules");
//        assertEquals("Schedule 1", response.getResult().get(0).getScheduleTitle(), "First schedule title should match");
//        assertEquals("Schedule 2", response.getResult().get(1).getScheduleTitle(), "Second schedule title should match");
//
//        assertNotNull(response.getAssetGroupDropdown(), "Asset group dropdown should not be null");
//        assertEquals(2, response.getAssetGroupDropdown().size(), "There should be 2 asset groups");
//        assertEquals("Asset Group 1", response.getAssetGroupDropdown().get(0).getName(), "First asset group name should match");
//        assertEquals("Asset Group 2", response.getAssetGroupDropdown().get(1).getName(), "Second asset group name should match");
//    }
//}
