//package com.organicnow.backend.unit.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MaintainDtoTest {
//
//    @Test
//    void testGetState_NotStarted() {
//        // Arrange
//        MaintainDto maintainDto = new MaintainDto();
//        maintainDto.setFinishDate(null); // สถานะจะเป็น "Not Started"
//
//        // Act
//        String state = maintainDto.getState(); // เรียกเมธอด getState()
//
//        // Assert
//        assertEquals("Not Started", state, "สถานะควรจะเป็น Not Started เมื่อ finishDate เป็น null");
//    }
//
//    @Test
//    void testGetState_Complete() {
//        // Arrange
//        MaintainDto maintainDto = new MaintainDto();
//        maintainDto.setFinishDate(LocalDateTime.now()); // สถานะจะเป็น "Complete"
//
//        // Act
//        String state = maintainDto.getState(); // เรียกเมธอด getState()
//
//        // Assert
//        assertEquals("Complete", state, "สถานะควรจะเป็น Complete เมื่อ finishDate ไม่เป็น null");
//    }
//}
//
