package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.UtilityUsageDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilityUsageDtoTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        UtilityUsageDto dto = new UtilityUsageDto();

        dto.setRoomNumber("202");
        dto.setWaterUsage(50);
        dto.setElectricityUsage(100);
        dto.setBillingMonth("2025-01");
        dto.setWaterRate(18);
        dto.setElectricityRate(7);

        assertEquals("202", dto.getRoomNumber());
        assertEquals(50, dto.getWaterUsage());
        assertEquals(100, dto.getElectricityUsage());
        assertEquals("2025-01", dto.getBillingMonth());
        assertEquals(18, dto.getWaterRate());
        assertEquals(7, dto.getElectricityRate());
    }

    @Test
    void testAllArgsConstructor() {
        UtilityUsageDto dto = new UtilityUsageDto(
                "305",
                60,
                120,
                "2025-02",
                20,
                8
        );

        assertEquals("305", dto.getRoomNumber());
        assertEquals(60, dto.getWaterUsage());
        assertEquals(120, dto.getElectricityUsage());
        assertEquals("2025-02", dto.getBillingMonth());
        assertEquals(20, dto.getWaterRate());
        assertEquals(8, dto.getElectricityRate());
    }

    @Test
    void testBuilder() {
        UtilityUsageDto dto = UtilityUsageDto.builder()
                .roomNumber("101A")
                .waterUsage(30)
                .electricityUsage(90)
                .billingMonth("2024-12")
                .waterRate(15)
                .electricityRate(5)
                .build();

        assertEquals("101A", dto.getRoomNumber());
        assertEquals(30, dto.getWaterUsage());
        assertEquals(90, dto.getElectricityUsage());
        assertEquals("2024-12", dto.getBillingMonth());
        assertEquals(15, dto.getWaterRate());
        assertEquals(5, dto.getElectricityRate());
    }

    @Test
    void testEqualsAndHashCodeShouldNotBeEqual() {
        UtilityUsageDto a = new UtilityUsageDto();
        UtilityUsageDto b = new UtilityUsageDto();

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }
}
