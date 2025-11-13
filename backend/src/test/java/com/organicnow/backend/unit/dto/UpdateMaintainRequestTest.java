//package com.organicnow.backend.unit.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class UpdateMaintainRequestTest {
//
//    @Test
//    void testUpdateMaintainRequestConstructorAndGettersSetters() {
//        // Arrange
//        Integer targetType = 1;          // ตัวอย่างค่าที่ไม่เป็น null
//        Long roomId = 101L;              // ตัวอย่างค่าที่ไม่เป็น null
//        String roomNumber = "A305";      // ตัวอย่างค่าที่ไม่เป็น null
//        Long roomAssetId = 200L;         // ตัวอย่างค่าที่ไม่เป็น null
//        Integer issueCategory = 2;       // ตัวอย่างค่าที่ไม่เป็น null
//        String issueTitle = "ไฟฟ้าดับ";  // ตัวอย่างค่าที่ไม่เป็น null
//        String issueDescription = "ไฟฟ้าดับในห้อง A305"; // ตัวอย่างค่าที่ไม่เป็น null
//        LocalDateTime scheduledDate = LocalDateTime.of(2025, 10, 20, 10, 0, 0, 0);
//        LocalDateTime finishDate = LocalDateTime.of(2025, 10, 21, 10, 0, 0, 0);
//
//        // Act: ใช้ Builder ในการสร้างอ็อบเจ็กต์
//        UpdateMaintainRequest request = UpdateMaintainRequest.builder()
//                .targetType(targetType)
//                .roomId(roomId)
//                .roomNumber(roomNumber)
//                .roomAssetId(roomAssetId)
//                .issueCategory(issueCategory)
//                .issueTitle(issueTitle)
//                .issueDescription(issueDescription)
//                .scheduledDate(scheduledDate)
//                .finishDate(finishDate)
//                .build();
//
//        // Assert: Verify that the values are correctly set
//        assertEquals(targetType, request.getTargetType(), "Target type should match");
//        assertEquals(roomId, request.getRoomId(), "Room ID should match");
//        assertEquals(roomNumber, request.getRoomNumber(), "Room number should match");
//        assertEquals(roomAssetId, request.getRoomAssetId(), "Room asset ID should match");
//        assertEquals(issueCategory, request.getIssueCategory(), "Issue category should match");
//        assertEquals(issueTitle, request.getIssueTitle(), "Issue title should match");
//        assertEquals(issueDescription, request.getIssueDescription(), "Issue description should match");
//        assertEquals(scheduledDate, request.getScheduledDate(), "Scheduled date should match");
//        assertEquals(finishDate, request.getFinishDate(), "Finish date should match");
//
//        // Testing with nullable fields (testing null values)
//        UpdateMaintainRequest nullRequest = new UpdateMaintainRequest();
//        assertNull(nullRequest.getTargetType(), "Target type should be null");
//        assertNull(nullRequest.getRoomId(), "Room ID should be null");
//        assertNull(nullRequest.getRoomNumber(), "Room number should be null");
//        assertNull(nullRequest.getRoomAssetId(), "Room asset ID should be null");
//        assertNull(nullRequest.getIssueCategory(), "Issue category should be null");
//        assertNull(nullRequest.getIssueTitle(), "Issue title should be null");
//        assertNull(nullRequest.getIssueDescription(), "Issue description should be null");
//        assertNull(nullRequest.getScheduledDate(), "Scheduled date should be null");
//        assertNull(nullRequest.getFinishDate(), "Finish date should be null");
//
//        // Testing setters for nullable fields
//        nullRequest.setRoomId(102L);
//        assertEquals(102L, nullRequest.getRoomId(), "Room ID should be updated");
//    }
//
//}
//
