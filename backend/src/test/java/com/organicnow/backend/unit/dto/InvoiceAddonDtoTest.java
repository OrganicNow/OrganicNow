package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.InvoiceAddonDto;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceAddonDtoTest {

    // ===========================================================
    // 1) Default constructor + setter/getter
    // ===========================================================
    @Test
    void testDefaultConstructorAndSetters() {
        InvoiceAddonDto dto = new InvoiceAddonDto();

        dto.setId(1L);
        dto.setInvoiceId(10L);
        dto.setAssetId(100L);
        dto.setAssetName("Air Conditioner");
        dto.setAssetGroupName("Electronics");
        dto.setAddonName("Extra Cleaning");
        dto.setQuantity(2);
        dto.setUnitPrice(BigDecimal.valueOf(150));
        dto.setTotalPrice(BigDecimal.valueOf(300));
        dto.setDescription("Monthly cleaning service");

        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getInvoiceId());
        assertEquals(100L, dto.getAssetId());
        assertEquals("Air Conditioner", dto.getAssetName());
        assertEquals("Electronics", dto.getAssetGroupName());
        assertEquals("Extra Cleaning", dto.getAddonName());
        assertEquals(2, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(150), dto.getUnitPrice());
        assertEquals(BigDecimal.valueOf(300), dto.getTotalPrice());
        assertEquals("Monthly cleaning service", dto.getDescription());
    }

    // ===========================================================
    // 2) AllArgsConstructor test
    // ===========================================================
    @Test
    void testAllArgsConstructor() {
        InvoiceAddonDto dto = new InvoiceAddonDto(
                2L,
                20L,
                200L,
                "TV",
                "Electronics",
                "Wall Mount",
                1,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(500),
                "TV installation"
        );

        assertEquals(2L, dto.getId());
        assertEquals(20L, dto.getInvoiceId());
        assertEquals(200L, dto.getAssetId());
        assertEquals("TV", dto.getAssetName());
        assertEquals("Electronics", dto.getAssetGroupName());
        assertEquals("Wall Mount", dto.getAddonName());
        assertEquals(1, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(500), dto.getUnitPrice());
        assertEquals(BigDecimal.valueOf(500), dto.getTotalPrice());
        assertEquals("TV installation", dto.getDescription());
    }

    // ===========================================================
    // 3) Builder test
    // ===========================================================
    @Test
    void testBuilder() {
        InvoiceAddonDto dto = InvoiceAddonDto.builder()
                .id(3L)
                .invoiceId(30L)
                .assetId(300L)
                .assetName("Washing Machine")
                .assetGroupName("Appliances")
                .addonName("Maintenance Plan")
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(200))
                .totalPrice(BigDecimal.valueOf(600))
                .description("Annual maintenance")
                .build();

        assertEquals(3L, dto.getId());
        assertEquals(30L, dto.getInvoiceId());
        assertEquals(300L, dto.getAssetId());
        assertEquals("Washing Machine", dto.getAssetName());
        assertEquals("Appliances", dto.getAssetGroupName());
        assertEquals("Maintenance Plan", dto.getAddonName());
        assertEquals(3, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(200), dto.getUnitPrice());
        assertEquals(BigDecimal.valueOf(600), dto.getTotalPrice());
        assertEquals("Annual maintenance", dto.getDescription());
    }

    // ===========================================================
    // 4) Helper Method: getDisplayName()
    // ===========================================================
    @Test
    void testGetDisplayName() {
        InvoiceAddonDto dto = InvoiceAddonDto.builder()
                .addonName("Extra Cleaning")
                .quantity(2)
                .build();

        assertEquals("Extra Cleaning (2 รายการ)", dto.getDisplayName());
    }

    // ===========================================================
    // 5) Helper Method: getFormattedTotal()
    // ===========================================================
    @Test
    void testGetFormattedTotal() {
        InvoiceAddonDto dto = InvoiceAddonDto.builder()
                .totalPrice(new BigDecimal("2500"))
                .build();

        assertEquals("2,500.00", dto.getFormattedTotal());
    }

    @Test
    void testGetFormattedTotalNull() {
        InvoiceAddonDto dto = new InvoiceAddonDto();
        assertEquals("0.00", dto.getFormattedTotal());
    }

    // ===========================================================
    // 6) Equals & HashCode
    // ===========================================================
    @Test
    void testEqualsAndHashCode() throws Exception {

        InvoiceAddonDto dto1 = InvoiceAddonDto.builder()
                .id(50L)
                .invoiceId(5L)
                .assetId(15L)
                .assetName("Air Purifier")
                .assetGroupName("Electronics")
                .addonName("Filter Replacement")
                .quantity(1)
                .unitPrice(new BigDecimal("800.00"))
                .totalPrice(new BigDecimal("800.00"))
                .description("Annual filter change")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ⭐⭐ สำคัญที่สุด — แก้ error displayName
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InvoiceAddonDto dto2 =
                mapper.readValue(mapper.writeValueAsString(dto1), InvoiceAddonDto.class);

        // เทียบ field ทีละตัว
        assertEquals(dto1.getId(), dto2.getId());
        assertEquals(dto1.getInvoiceId(), dto2.getInvoiceId());
        assertEquals(dto1.getAssetId(), dto2.getAssetId());
        assertEquals(dto1.getAssetName(), dto2.getAssetName());
        assertEquals(dto1.getAssetGroupName(), dto2.getAssetGroupName());
        assertEquals(dto1.getAddonName(), dto2.getAddonName());
        assertEquals(dto1.getQuantity(), dto2.getQuantity());
        assertEquals(dto1.getDescription(), dto2.getDescription());

        // BigDecimal → compareTo()
        assertEquals(0, dto1.getUnitPrice().compareTo(dto2.getUnitPrice()));
        assertEquals(0, dto1.getTotalPrice().compareTo(dto2.getTotalPrice()));
    }

    // ===========================================================
    // 7) toString() should not be null
    // ===========================================================
    @Test
    void testToString() {
        InvoiceAddonDto dto = new InvoiceAddonDto();
        assertNotNull(dto.toString());
    }

    // ===========================================================
    // 8) Null Safety
    // ===========================================================
    @Test
    void testNullSafety() {
        InvoiceAddonDto dto = new InvoiceAddonDto();

        dto.setAddonName(null);
        dto.setUnitPrice(null);
        dto.setTotalPrice(null);
        dto.setDescription(null);

        assertNull(dto.getAddonName());
        assertNull(dto.getUnitPrice());
        assertNull(dto.getTotalPrice());
        assertNull(dto.getDescription());
    }
}
