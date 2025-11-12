//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MaintenanceScheduleDtoTest {
//
//    @Test
//    void testMaintenanceScheduleDtoConstructorAndGettersSetters() {
//        // Arrange
//        Long id = 1L;
//        Integer scheduleScope = 0;
//        Long assetGroupId = 101L;
//        String assetGroupName = "Asset Group 1";
//        Integer cycleMonth = 6;
//        LocalDateTime now = LocalDateTime.now(); // ใช้เวลาปัจจุบัน
//        Integer notifyBeforeDate = 5;
//        String scheduleTitle = "Routine Maintenance";
//        String scheduleDescription = "Maintenance every 6 months";
//
//        // Act: สร้างอ็อบเจ็กต์ MaintenanceScheduleDto
//        MaintenanceScheduleDto dto = MaintenanceScheduleDto.builder()
//                .id(id)
//                .scheduleScope(scheduleScope)
//                .assetGroupId(assetGroupId)
//                .assetGroupName(assetGroupName)
//                .cycleMonth(cycleMonth)
//                .lastDoneDate(now)
//                .nextDueDate(now.plusMonths(6)) // กำหนด nextDueDate เป็น 6 เดือนถัดไป
//                .notifyBeforeDate(notifyBeforeDate)
//                .scheduleTitle(scheduleTitle)
//                .scheduleDescription(scheduleDescription)
//                .build();
//
//        // Assert: ตรวจสอบการตั้งค่าที่ถูกต้อง
//        assertEquals(id, dto.getId(), "ID should match");
//        assertEquals(scheduleScope, dto.getScheduleScope(), "Schedule scope should match");
//        assertEquals(assetGroupId, dto.getAssetGroupId(), "Asset group ID should match");
//        assertEquals(assetGroupName, dto.getAssetGroupName(), "Asset group name should match");
//        assertEquals(cycleMonth, dto.getCycleMonth(), "Cycle month should match");
//        assertEquals(now, dto.getLastDoneDate(), "Last done date should match");
//        assertEquals(now.plusMonths(6), dto.getNextDueDate(), "Next due date should be 6 months after last done date");
//        assertEquals(notifyBeforeDate, dto.getNotifyBeforeDate(), "Notify before date should match");
//        assertEquals(scheduleTitle, dto.getScheduleTitle(), "Schedule title should match");
//        assertEquals(scheduleDescription, dto.getScheduleDescription(), "Schedule description should match");
//
//        // Test setters for nullable fields (optional)
//        MaintenanceScheduleDto nullRequest = new MaintenanceScheduleDto();
//        assertNull(nullRequest.getAssetGroupName(), "Asset group name should be null by default");
//        assertNull(nullRequest.getScheduleTitle(), "Schedule title should be null by default");
//        assertNull(nullRequest.getScheduleDescription(), "Schedule description should be null by default");
//        assertNull(nullRequest.getLastDoneDate(), "Last done date should be null by default");
//        assertNull(nullRequest.getNextDueDate(), "Next due date should be null by default");
//    }
//}
