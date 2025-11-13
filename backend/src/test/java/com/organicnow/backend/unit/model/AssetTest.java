package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AssetTest {

    @Test
    void testGetterAndSetter() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssetName("Air Conditioner");
        asset.setStatus("available");

        assertEquals(1L, asset.getId());
        assertEquals("Air Conditioner", asset.getAssetName());
        assertEquals("available", asset.getStatus());
    }

    @Test
    void testRelationshipWithAssetGroup() {
        AssetGroup group = new AssetGroup();
        group.setId(100L);
        group.setAssetGroupName("Electrical");
        group.setMonthlyAddonFee(new BigDecimal("50.00"));
        group.setFreeReplacement(false);

        Asset asset = new Asset();
        asset.setAssetGroup(group);

        assertNotNull(asset.getAssetGroup());
        assertEquals(100L, asset.getAssetGroup().getId());
        assertEquals("Electrical", asset.getAssetGroup().getAssetGroupName());
        assertEquals(new BigDecimal("50.00"), asset.getAssetGroup().getMonthlyAddonFee());
        assertFalse(asset.getAssetGroup().getFreeReplacement());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        AssetGroup group = AssetGroup.builder()
                .id(10L)
                .assetGroupName("Furniture")
                .monthlyAddonFee(new BigDecimal("250.00"))
                .build();

        Asset asset = Asset.builder()
                .id(2L)
                .assetGroup(group)
                .assetName("Table")
                .status("in_use")
                .build();

        assertNotNull(asset);
        assertEquals(2L, asset.getId());
        assertEquals("Table", asset.getAssetName());
        assertEquals("in_use", asset.getStatus());
        assertEquals("Furniture", asset.getAssetGroup().getAssetGroupName());
        assertEquals(new BigDecimal("250.00"), asset.getAssetGroup().getMonthlyAddonFee());
    }

    @Test
    void testAllArgsConstructor() {
        AssetGroup group = new AssetGroup();
        group.setId(200L);
        group.setAssetGroupName("Appliances");

        Asset asset = new Asset(5L, group, "Printer", "maintenance");

        assertEquals(5L, asset.getId());
        assertEquals("Printer", asset.getAssetName());
        assertEquals("maintenance", asset.getStatus());
        assertEquals("Appliances", asset.getAssetGroup().getAssetGroupName());
    }

    @Test
    void testToStringNotNull() {
        Asset asset = new Asset();
        asset.setAssetName("Desk");
        assertNotNull(asset.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Asset a1 = new Asset();
        Asset a3 = new Asset();

        assertEquals(a1, a1);   // อ้างอิงเดียวกัน
        assertNotEquals(a1, a3); // คนละอ้างอิง
    }
}
