package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.FinanceMonthlyDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinanceMonthlyDtoTest {

    // ===========================================================
    // 1) Default constructor + setters + getters
    // ===========================================================
    @Test
    void testDefaultConstructorAndSetters() {
        FinanceMonthlyDto dto = new FinanceMonthlyDto();

        dto.setMonth("2024-01");
        dto.setOnTime(100L);
        dto.setPenalty(20L);
        dto.setOverdue(5L);

        assertEquals("2024-01", dto.getMonth());
        assertEquals(100L, dto.getOnTime());
        assertEquals(20L, dto.getPenalty());
        assertEquals(5L, dto.getOverdue());
    }

    // ===========================================================
    // 2) AllArgsConstructor
    // ===========================================================
    @Test
    void testAllArgsConstructor() {
        FinanceMonthlyDto dto = new FinanceMonthlyDto(
                "2024-02",
                120L,
                15L,
                10L
        );

        assertEquals("2024-02", dto.getMonth());
        assertEquals(120L, dto.getOnTime());
        assertEquals(15L, dto.getPenalty());
        assertEquals(10L, dto.getOverdue());
    }

    // ===========================================================
    // 3) Equals & HashCode
    // ===========================================================
    @Test
    void testEqualsAndHashCode() {
        FinanceMonthlyDto dto1 = new FinanceMonthlyDto("2024-03", 50L, 8L, 2L);
        FinanceMonthlyDto dto2 = new FinanceMonthlyDto("2024-03", 50L, 8L, 2L);

        assertEquals(dto1, dto2, "DTO objects must match");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Hashcodes must match");
    }

    // ===========================================================
    // 4) toString() should not be null
    // ===========================================================
    @Test
    void testToStringNotNull() {
        FinanceMonthlyDto dto = new FinanceMonthlyDto();
        assertNotNull(dto.toString());
    }

    // ===========================================================
    // 5) Null safety
    // ===========================================================
    @Test
    void testNullValues() {
        FinanceMonthlyDto dto = new FinanceMonthlyDto();

        dto.setMonth(null);
        dto.setOnTime(null);
        dto.setPenalty(null);
        dto.setOverdue(null);

        assertNull(dto.getMonth());
        assertNull(dto.getOnTime());
        assertNull(dto.getPenalty());
        assertNull(dto.getOverdue());
    }
}
