//package com.organicnow.backend.unit.dto;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class FinanceMonthlyDtoTest {
//
//    @Test
//    void testNoArgsConstructor() {
//        // Act
//        FinanceMonthlyDto financeMonthlyDto = new FinanceMonthlyDto();
//
//        // Assert
//        assertNull(financeMonthlyDto.getMonth());
//        assertNull(financeMonthlyDto.getOnTime());
//        assertNull(financeMonthlyDto.getPenalty());
//        assertNull(financeMonthlyDto.getOverdue());
//    }
//
//    @Test
//    void testAllArgsConstructor() {
//        // Arrange
//        String expectedMonth = "2025-03";
//        Long expectedOnTime = 100L;
//        Long expectedPenalty = 20L;
//        Long expectedOverdue = 5L;
//
//        // Act
//        FinanceMonthlyDto financeMonthlyDto = new FinanceMonthlyDto(expectedMonth, expectedOnTime, expectedPenalty, expectedOverdue);
//
//        // Assert
//        assertEquals(expectedMonth, financeMonthlyDto.getMonth());
//        assertEquals(expectedOnTime, financeMonthlyDto.getOnTime());
//        assertEquals(expectedPenalty, financeMonthlyDto.getPenalty());
//        assertEquals(expectedOverdue, financeMonthlyDto.getOverdue());
//    }
//
//    @Test
//    void testSettersAndGetters() {
//        // Arrange
//        FinanceMonthlyDto financeMonthlyDto = new FinanceMonthlyDto();
//        String expectedMonth = "2025-04";
//        Long expectedOnTime = 120L;
//        Long expectedPenalty = 30L;
//        Long expectedOverdue = 10L;
//
//        // Act
//        financeMonthlyDto.setMonth(expectedMonth);
//        financeMonthlyDto.setOnTime(expectedOnTime);
//        financeMonthlyDto.setPenalty(expectedPenalty);
//        financeMonthlyDto.setOverdue(expectedOverdue);
//
//        // Assert
//        assertEquals(expectedMonth, financeMonthlyDto.getMonth());
//        assertEquals(expectedOnTime, financeMonthlyDto.getOnTime());
//        assertEquals(expectedPenalty, financeMonthlyDto.getPenalty());
//        assertEquals(expectedOverdue, financeMonthlyDto.getOverdue());
//    }
//
//    @Test
//    void testToString() {
//        // Arrange
//        FinanceMonthlyDto financeMonthlyDto = new FinanceMonthlyDto("2025-03", 100L, 20L, 5L);
//
//        // Act & Assert
//        String expectedToString = "FinanceMonthlyDto(month=2025-03, onTime=100, penalty=20, overdue=5)";
//        assertEquals(expectedToString, financeMonthlyDto.toString());
//    }
//}
