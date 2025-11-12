package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.AssetGroup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssetGroupTest {

    @Test
    void testGetterAndSetter() {
        AssetGroup group = new AssetGroup();
        group.setId(1L);
        group.setAssetGroupName("Furniture");
        group.setMonthlyAddonFee(new BigDecimal("150.00"));
        group.setOneTimeDamageFee(new BigDecimal("500.00"));
        group.setFreeReplacement(false);

        assertEquals(1L, group.getId());
        assertEquals("Furniture", group.getAssetGroupName());
        assertEquals(new BigDecimal("150.00"), group.getMonthlyAddonFee());
        assertEquals(new BigDecimal("500.00"), group.getOneTimeDamageFee());
        assertFalse(group.getFreeReplacement());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        AssetGroup group = AssetGroup.builder()
                .id(2L)
                .assetGroupName("Electrical")
                .monthlyAddonFee(new BigDecimal("50.00"))
                .oneTimeDamageFee(new BigDecimal("200.00"))
                .freeReplacement(true)
                .build();

        assertNotNull(group);
        assertEquals(2L, group.getId());
        assertEquals("Electrical", group.getAssetGroupName());
        assertEquals(new BigDecimal("50.00"), group.getMonthlyAddonFee());
        assertEquals(new BigDecimal("200.00"), group.getOneTimeDamageFee());
        assertTrue(group.getFreeReplacement());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.of(2025, 11, 13, 10, 0);
        AssetGroup group = new AssetGroup(
                3L,
                "Appliances",
                new BigDecimal("250.00"),
                new BigDecimal("800.00"),
                false,
                now
        );

        assertEquals(3L, group.getId());
        assertEquals("Appliances", group.getAssetGroupName());
        assertEquals(new BigDecimal("250.00"), group.getMonthlyAddonFee());
        assertEquals(new BigDecimal("800.00"), group.getOneTimeDamageFee());
        assertFalse(group.getFreeReplacement());
        assertEquals(now, group.getUpdatedAt());
    }

    @Test
    void testPreUpdateSetsUpdatedAt() throws InterruptedException {
        AssetGroup group = new AssetGroup();
        LocalDateTime before = group.getUpdatedAt();

        // 🕒 รอให้เวลาผ่านเล็กน้อย เพื่อให้ LocalDateTime.now() แตกต่างกันจริง
        Thread.sleep(10);

        group.preUpdate();
        LocalDateTime after = group.getUpdatedAt();

        // 🔍 ตรวจว่าค่าใหม่ไม่เท่าค่าเก่า
        assertTrue(after.isAfter(before),
                "expected updatedAt to be after previous timestamp");
    }


    @Test
    void testToStringNotNull() {
        AssetGroup group = new AssetGroup();
        group.setAssetGroupName("Decor");
        assertNotNull(group.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        AssetGroup g1 = new AssetGroup();
        AssetGroup g3 = new AssetGroup();

        assertSame(g1, g1);   // อ้างอิงเดียวกัน
        assertNotSame(g1, g3); // คนละอ้างอิง
    }
}
