//package com.organicnow.backend.unit.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MaintenanceScheduleCreateDtoTest {
//
//    @Test
//    void testMaintenanceScheduleCreateDtoConstructorAndGettersSetters() {
//        // Arrange
//        LocalDateTime now = LocalDateTime.now(); // ใช้เวลาปัจจุบัน
//
//        Integer scheduleScope = 1;
//        Long assetGroupId = 2L;
//        Integer cycleMonth = 6;
//        Integer notifyBeforeDate = 7;
//        String scheduleTitle = "Routine Maintenance";
//        String scheduleDescription = "Monthly maintenance of all assets";
//
//        // สร้าง MaintenanceScheduleCreateDto
//        MaintenanceScheduleCreateDto dto = MaintenanceScheduleCreateDto.builder()
//                .scheduleScope(scheduleScope)
//                .assetGroupId(assetGroupId)
//                .cycleMonth(cycleMonth)
//                .notifyBeforeDate(notifyBeforeDate)
//                .scheduleTitle(scheduleTitle)
//                .scheduleDescription(scheduleDescription)
//                .lastDoneDate(now)
//                .nextDueDate(now.plusMonths(1)) // กำหนด nextDueDate เป็นเดือนถัดไป
//                .build();
//
//        // Act and Assert: ตรวจสอบการตั้งค่าที่ถูกต้อง
//        assertEquals(scheduleScope, dto.getScheduleScope(), "Schedule scope should match");
//        assertEquals(assetGroupId, dto.getAssetGroupId(), "Asset group ID should match");
//        assertEquals(cycleMonth, dto.getCycleMonth(), "Cycle month should match");
//        assertEquals(notifyBeforeDate, dto.getNotifyBeforeDate(), "Notify before date should match");
//        assertEquals(scheduleTitle, dto.getScheduleTitle(), "Schedule title should match");
//        assertEquals(scheduleDescription, dto.getScheduleDescription(), "Schedule description should match");
//
//        assertEquals(now, dto.getLastDoneDate(), "Last done date should match");
//        assertEquals(now.plusMonths(1), dto.getNextDueDate(), "Next due date should be one month ahead of last done date");
//
//        // Test setters for nullable fields
//        MaintenanceScheduleCreateDto nullRequest = new MaintenanceScheduleCreateDto();
//        assertNull(nullRequest.getScheduleScope(), "Schedule scope should be null by default");
//        assertNull(nullRequest.getAssetGroupId(), "Asset group ID should be null by default");
//        assertNull(nullRequest.getCycleMonth(), "Cycle month should be null by default");
//        assertNull(nullRequest.getNotifyBeforeDate(), "Notify before date should be null by default");
//        assertNull(nullRequest.getScheduleTitle(), "Schedule title should be null by default");
//        assertNull(nullRequest.getScheduleDescription(), "Schedule description should be null by default");
//        assertNull(nullRequest.getLastDoneDate(), "Last done date should be null by default");
//        assertNull(nullRequest.getNextDueDate(), "Next due date should be null by default");
//    }
//}
