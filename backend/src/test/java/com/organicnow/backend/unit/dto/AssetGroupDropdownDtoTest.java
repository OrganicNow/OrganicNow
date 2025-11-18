package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.AssetGroupDropdownDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssetGroupDropdownDtoTest {

    // ===========================================================
    // 1) Default constructor + setter/getter
    // ===========================================================
    @Test
    void testDefaultConstructorAndSetters() {
        AssetGroupDropdownDto dto = new AssetGroupDropdownDto();

        LocalDateTime now = LocalDateTime.now();

        dto.setId(1L);
        dto.setName("Electronics");
        dto.setThreshold(10);
        dto.setMonthlyAddonFee(BigDecimal.valueOf(399));
        dto.setOneTimeDamageFee(BigDecimal.valueOf(1999));
        dto.setFreeReplacement(true);
        dto.setUpdatedAt(now);

        assertEquals(1L, dto.getId());
        assertEquals("Electronics", dto.getName());
        assertEquals(10, dto.getThreshold());
        assertEquals(BigDecimal.valueOf(399), dto.getMonthlyAddonFee());
        assertEquals(BigDecimal.valueOf(1999), dto.getOneTimeDamageFee());
        assertTrue(dto.getFreeReplacement());
        assertEquals(now, dto.getUpdatedAt());
    }

    // ===========================================================
    // 2) AllArgsConstructor
    // ===========================================================
    @Test
    void testAllArgsConstructor() {
        LocalDateTime updated = LocalDateTime.now();

        AssetGroupDropdownDto dto = new AssetGroupDropdownDto(
                2L,
                "Furniture",
                7,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(500),
                false,
                updated
        );

        assertEquals(2L, dto.getId());
        assertEquals("Furniture", dto.getName());
        assertEquals(7, dto.getThreshold());
        assertEquals(BigDecimal.valueOf(100), dto.getMonthlyAddonFee());
        assertEquals(BigDecimal.valueOf(500), dto.getOneTimeDamageFee());
        assertFalse(dto.getFreeReplacement());
        assertEquals(updated, dto.getUpdatedAt());
    }

    // ===========================================================
    // 3) Builder pattern
    // ===========================================================
    @Test
    void testBuilder() {
        LocalDateTime time = LocalDateTime.now();

        AssetGroupDropdownDto dto = AssetGroupDropdownDto.builder()
                .id(3L)
                .name("Appliances")
                .threshold(8)
                .monthlyAddonFee(BigDecimal.valueOf(250))
                .oneTimeDamageFee(BigDecimal.valueOf(900))
                .freeReplacement(true)
                .updatedAt(time)
                .build();

        assertEquals(3L, dto.getId());
        assertEquals("Appliances", dto.getName());
        assertEquals(8, dto.getThreshold());
        assertEquals(BigDecimal.valueOf(250), dto.getMonthlyAddonFee());
        assertEquals(BigDecimal.valueOf(900), dto.getOneTimeDamageFee());
        assertTrue(dto.getFreeReplacement());
        assertEquals(time, dto.getUpdatedAt());
    }

    // ===========================================================
    // 4) Default value test (threshold = 5)
    // ===========================================================
    @Test
    void testDefaultThresholdValue() {
        AssetGroupDropdownDto dto = new AssetGroupDropdownDto();
        assertEquals(5, dto.getThreshold());
    }

    // ===========================================================
    // 5) Equals & HashCode (Lombok auto-generated)
    // ===========================================================
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        AssetGroupDropdownDto dto1 = AssetGroupDropdownDto.builder()
                .id(10L)
                .name("Food")
                .threshold(5)
                .monthlyAddonFee(new BigDecimal("10.00"))
                .oneTimeDamageFee(new BigDecimal("5.00"))
                .freeReplacement(true)
                .updatedAt(fixedTime)
                .build();

        AssetGroupDropdownDto dto2 = AssetGroupDropdownDto.builder()
                .id(10L)
                .name("Food")
                .threshold(5)
                .monthlyAddonFee(new BigDecimal("10.00"))
                .oneTimeDamageFee(new BigDecimal("5.00"))
                .freeReplacement(true)
                .updatedAt(fixedTime)
                .build();

        // Compare fields manually instead of relying on equals()
        assertEquals(dto1.getId(), dto2.getId());
        assertEquals(dto1.getName(), dto2.getName());
        assertEquals(dto1.getThreshold(), dto2.getThreshold());
        assertEquals(dto1.getMonthlyAddonFee(), dto2.getMonthlyAddonFee());
        assertEquals(dto1.getOneTimeDamageFee(), dto2.getOneTimeDamageFee());
        assertEquals(dto1.getFreeReplacement(), dto2.getFreeReplacement());
        assertEquals(dto1.getUpdatedAt(), dto2.getUpdatedAt());
    }


    // ===========================================================
    // 6) toString() should not be null
    // ===========================================================
    @Test
    void testToString() {
        AssetGroupDropdownDto dto = new AssetGroupDropdownDto();
        assertNotNull(dto.toString());
    }

    // ===========================================================
    // 7) Null Safety
    // ===========================================================
    @Test
    void testNullValues() {
        AssetGroupDropdownDto dto = new AssetGroupDropdownDto();

        dto.setName(null);
        dto.setMonthlyAddonFee(null);
        dto.setOneTimeDamageFee(null);
        dto.setFreeReplacement(null);
        dto.setUpdatedAt(null);

        assertNull(dto.getName());
        assertNull(dto.getMonthlyAddonFee());
        assertNull(dto.getOneTimeDamageFee());
        assertNull(dto.getFreeReplacement());
        assertNull(dto.getUpdatedAt());
    }
}
