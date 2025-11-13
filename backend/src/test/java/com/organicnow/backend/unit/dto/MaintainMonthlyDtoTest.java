//package com.organicnow.backend.unit.dto;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MaintainMonthlyDtoTest {
//
//    @Test
//    void testMaintainMonthlyDtoConstructor() {
//        // Arrange
//        String month = "2025-10";
//        Long total = 100L;
//
//        // Act
//        MaintainMonthlyDto maintainMonthlyDto = new MaintainMonthlyDto(month, total);
//
//        // Assert
//        assertEquals(month, maintainMonthlyDto.getMonth(), "Month should match the expected value");
//        assertEquals(total, maintainMonthlyDto.getTotal(), "Total should match the expected value");
//    }
//
//    @Test
//    void testMaintainMonthlyDtoNoArgsConstructor() {
//        // Act
//        MaintainMonthlyDto maintainMonthlyDto = new MaintainMonthlyDto();
//
//        // Assert
//        assertNull(maintainMonthlyDto.getMonth(), "Month should be null by default");
//        assertNull(maintainMonthlyDto.getTotal(), "Total should be null by default");
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        MaintainMonthlyDto maintainMonthlyDto = new MaintainMonthlyDto();
//        String month = "2025-10";
//        Long total = 50L;
//
//        // Act
//        maintainMonthlyDto.setMonth(month);
//        maintainMonthlyDto.setTotal(total);
//
//        // Assert
//        assertEquals(month, maintainMonthlyDto.getMonth(), "Month should be updated correctly");
//        assertEquals(total, maintainMonthlyDto.getTotal(), "Total should be updated correctly");
//    }
//}
