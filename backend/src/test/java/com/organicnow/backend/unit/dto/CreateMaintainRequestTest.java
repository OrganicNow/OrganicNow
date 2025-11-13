//package com.organicnow.backend.unit.dto;
//
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class CreateMaintainRequestTest {
//
//    @Test
//    void testNoArgsConstructor() {
//        // Act
//        CreateMaintainRequest request = new CreateMaintainRequest();
//
//        // Assert
//        assertNull(request.getTargetType());
//        assertNull(request.getRoomId());
//        assertNull(request.getRoomNumber());
//        assertNull(request.getRoomAssetId());
//        assertNull(request.getIssueCategory());
//        assertNull(request.getIssueTitle());
//        assertNull(request.getIssueDescription());
//        assertNull(request.getCreateDate());
//        assertNull(request.getScheduledDate());
//        assertNull(request.getFinishDate());
//    }
//
//    @Test
//    void testAllArgsConstructor() {
//        // Arrange
//        Integer expectedTargetType = 1;
//        Long expectedRoomId = 101L;
//        String expectedRoomNumber = "A101";
//        Long expectedRoomAssetId = 202L;
//        Integer expectedIssueCategory = 3;
//        String expectedIssueTitle = "Air Conditioner not working";
//        String expectedIssueDescription = "The AC is not cooling properly";
//        LocalDateTime expectedCreateDate = LocalDateTime.now();
//        LocalDateTime expectedScheduledDate = LocalDateTime.of(2025, 5, 1, 10, 0, 0, 0);
//        LocalDateTime expectedFinishDate = LocalDateTime.of(2025, 5, 2, 10, 0, 0, 0);
//
//        // Act: ใช้ Builder แทน constructor
//        CreateMaintainRequest request = CreateMaintainRequest.builder()
//                .targetType(expectedTargetType)
//                .roomId(expectedRoomId)
//                .roomNumber(expectedRoomNumber)
//                .roomAssetId(expectedRoomAssetId)
//                .issueCategory(expectedIssueCategory)
//                .issueTitle(expectedIssueTitle)
//                .issueDescription(expectedIssueDescription)
//                .createDate(expectedCreateDate)
//                .scheduledDate(expectedScheduledDate)
//                .finishDate(expectedFinishDate)
//                .build();
//
//        // Assert
//        assertEquals(expectedTargetType, request.getTargetType());
//        assertEquals(expectedRoomId, request.getRoomId());
//        assertEquals(expectedRoomNumber, request.getRoomNumber());
//        assertEquals(expectedRoomAssetId, request.getRoomAssetId());
//        assertEquals(expectedIssueCategory, request.getIssueCategory());
//        assertEquals(expectedIssueTitle, request.getIssueTitle());
//        assertEquals(expectedIssueDescription, request.getIssueDescription());
//        assertEquals(expectedCreateDate, request.getCreateDate());
//        assertEquals(expectedScheduledDate, request.getScheduledDate());
//        assertEquals(expectedFinishDate, request.getFinishDate());
//    }
//
//
//    @Test
//    void testBuilder() {
//        // Arrange
//        Integer expectedTargetType = 2;
//        Long expectedRoomId = 102L;
//        String expectedRoomNumber = "B102";
//        Long expectedRoomAssetId = 203L;
//        Integer expectedIssueCategory = 1;
//        String expectedIssueTitle = "Water leakage in bathroom";
//        String expectedIssueDescription = "The water supply is leaking from the pipe";
//        LocalDateTime expectedCreateDate = LocalDateTime.now();
//        LocalDateTime expectedScheduledDate = LocalDateTime.of(2025, 5, 5, 9, 0, 0, 0);
//        LocalDateTime expectedFinishDate = LocalDateTime.of(2025, 5, 6, 9, 0, 0, 0);
//
//        // Act
//        CreateMaintainRequest request = CreateMaintainRequest.builder()
//                .targetType(expectedTargetType)
//                .roomId(expectedRoomId)
//                .roomNumber(expectedRoomNumber)
//                .roomAssetId(expectedRoomAssetId)
//                .issueCategory(expectedIssueCategory)
//                .issueTitle(expectedIssueTitle)
//                .issueDescription(expectedIssueDescription)
//                .createDate(expectedCreateDate)
//                .scheduledDate(expectedScheduledDate)
//                .finishDate(expectedFinishDate)
//                .build();
//
//        // Assert
//        assertEquals(expectedTargetType, request.getTargetType());
//        assertEquals(expectedRoomId, request.getRoomId());
//        assertEquals(expectedRoomNumber, request.getRoomNumber());
//        assertEquals(expectedRoomAssetId, request.getRoomAssetId());
//        assertEquals(expectedIssueCategory, request.getIssueCategory());
//        assertEquals(expectedIssueTitle, request.getIssueTitle());
//        assertEquals(expectedIssueDescription, request.getIssueDescription());
//        assertEquals(expectedCreateDate, request.getCreateDate());
//        assertEquals(expectedScheduledDate, request.getScheduledDate());
//        assertEquals(expectedFinishDate, request.getFinishDate());
//    }
//
//    @Test
//    void testSettersAndGetters() {
//        // Arrange
//        CreateMaintainRequest request = new CreateMaintainRequest();
//        Integer expectedTargetType = 4;
//        String expectedIssueTitle = "Power outage";
//
//        // Act
//        request.setTargetType(expectedTargetType);
//        request.setIssueTitle(expectedIssueTitle);
//
//        // Assert
//        assertEquals(expectedTargetType, request.getTargetType());
//        assertEquals(expectedIssueTitle, request.getIssueTitle());
//    }
//}
