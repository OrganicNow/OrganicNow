//package com.organicnow.backend.unit.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class RequestDtoTest {
//
//    @Test
//    void testRequestDtoConstructor() {
//        // Arrange
//        Long id = 1L;
//        String issueTitle = "HVAC System Issue";
//        String issueDescription = "The HVAC system is not working properly.";
//        LocalDateTime createDate = LocalDateTime.of(2025, 10, 14, 12, 0, 0, 0);
//        LocalDateTime scheduledDate = LocalDateTime.of(2025, 10, 20, 9, 0, 0, 0);
//        LocalDateTime finishDate = LocalDateTime.of(2025, 10, 22, 17, 0, 0, 0);
//
//        // Act
//        RequestDto dto = new RequestDto(id, issueTitle, issueDescription, createDate, scheduledDate, finishDate);
//
//        // Assert
//        assertEquals(id, dto.getId());
//        assertEquals(issueTitle, dto.getIssueTitle());
//        assertEquals(issueDescription, dto.getIssueDescription());
//        assertEquals(createDate, dto.getCreateDate());
//        assertEquals(scheduledDate, dto.getScheduledDate());
//        assertEquals(finishDate, dto.getFinishDate());
//    }
//
//    @Test
//    void testRequestDtoQueryConstructor() {
//        // Arrange
//        Long id = 2L;
//        String issueTitle = "Plumbing Issue";
//        LocalDateTime scheduledDate = LocalDateTime.of(2025, 10, 18, 9, 0, 0, 0);
//        LocalDateTime finishDate = LocalDateTime.of(2025, 10, 19, 16, 0, 0, 0);
//
//        // Act
//        RequestDto dto = new RequestDto(id, issueTitle, scheduledDate, finishDate);
//
//        // Assert
//        assertEquals(id, dto.getId());
//        assertEquals(issueTitle, dto.getIssueTitle());
//        assertEquals(scheduledDate, dto.getScheduledDate());
//        assertEquals(finishDate, dto.getFinishDate());
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        RequestDto dto = new RequestDto();
//        Long id = 3L;
//        String issueTitle = "Electrical Issue";
//        String issueDescription = "Lights are flickering in the room.";
//        LocalDateTime createDate = LocalDateTime.of(2025, 10, 12, 10, 0, 0, 0);
//        LocalDateTime scheduledDate = LocalDateTime.of(2025, 10, 15, 14, 0, 0, 0);
//        LocalDateTime finishDate = LocalDateTime.of(2025, 10, 16, 12, 0, 0, 0);
//
//        // Act
//        dto.setId(id);
//        dto.setIssueTitle(issueTitle);
//        dto.setIssueDescription(issueDescription);
//        dto.setCreateDate(createDate);
//        dto.setScheduledDate(scheduledDate);
//        dto.setFinishDate(finishDate);
//
//        // Assert
//        assertEquals(id, dto.getId());
//        assertEquals(issueTitle, dto.getIssueTitle());
//        assertEquals(issueDescription, dto.getIssueDescription());
//        assertEquals(createDate, dto.getCreateDate());
//        assertEquals(scheduledDate, dto.getScheduledDate());
//        assertEquals(finishDate, dto.getFinishDate());
//    }
//}
//
