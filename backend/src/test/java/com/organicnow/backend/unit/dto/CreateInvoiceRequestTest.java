package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.CreateInvoiceRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateInvoiceRequestTest {

    // ============================================================
    // 1) Default constructor + setter/getter
    // ============================================================
    @Test
    void testDefaultConstructorAndSetters() {
        CreateInvoiceRequest dto = new CreateInvoiceRequest();

        LocalDateTime due = LocalDateTime.now();

        dto.setContractId(10L);
        dto.setDueDate(due);
        dto.setSubTotal(2000);
        dto.setPenaltyTotal(150);
        dto.setNetAmount(2150);
        dto.setNotes("Test notes");

        dto.setRentAmount(1000);
        dto.setWaterUnit(5);
        dto.setWaterRate(20);
        dto.setElectricityUnit(10);
        dto.setElectricityRate(7);

        dto.setCreateDate("2025-02-28");
        dto.setInvoiceStatus(1);

        dto.setWater(120);
        dto.setElectricity(350);
        dto.setElecUnit(10);

        dto.setPackageId(99L);
        dto.setFloor("3");
        dto.setRoom("305");
        dto.setIncludeOutstandingBalance(true);

        assertEquals(10L, dto.getContractId());
        assertEquals(due, dto.getDueDate());
        assertEquals(2000, dto.getSubTotal());
        assertEquals(150, dto.getPenaltyTotal());
        assertEquals(2150, dto.getNetAmount());
        assertEquals("Test notes", dto.getNotes());

        assertEquals(1000, dto.getRentAmount());
        assertEquals(5, dto.getWaterUnit());
        assertEquals(20, dto.getWaterRate());
        assertEquals(10, dto.getElectricityUnit());
        assertEquals(7, dto.getElectricityRate());

        assertEquals("2025-02-28", dto.getCreateDate());
        assertEquals(1, dto.getInvoiceStatus());

        assertEquals(120, dto.getWater());
        assertEquals(350, dto.getElectricity());
        assertEquals(10, dto.getElecUnit());

        assertEquals(99L, dto.getPackageId());
        assertEquals("3", dto.getFloor());
        assertEquals("305", dto.getRoom());
        assertTrue(dto.getIncludeOutstandingBalance());
    }

    // ============================================================
    // 2) AllArgsConstructor
    // ============================================================
    @Test
    void testAllArgsConstructor() {
        LocalDateTime due = LocalDateTime.now();

        CreateInvoiceRequest dto = new CreateInvoiceRequest(
                1L, due, 1000, 0, 1000, "note",
                500, 5, 20, 10, 7,
                "2025-01-01", 1,
                50, 250, 10,
                88L, "2", "201",
                false
        );

        assertEquals(1L, dto.getContractId());
        assertEquals(due, dto.getDueDate());
        assertEquals(1000, dto.getSubTotal());
        assertEquals(0, dto.getPenaltyTotal());
        assertEquals(1000, dto.getNetAmount());
        assertEquals("note", dto.getNotes());

        assertEquals(500, dto.getRentAmount());
        assertEquals(5, dto.getWaterUnit());
        assertEquals(20, dto.getWaterRate());
        assertEquals(10, dto.getElectricityUnit());
        assertEquals(7, dto.getElectricityRate());

        assertEquals("2025-01-01", dto.getCreateDate());
        assertEquals(1, dto.getInvoiceStatus());

        assertEquals(50, dto.getWater());
        assertEquals(250, dto.getElectricity());
        assertEquals(10, dto.getElecUnit());

        assertEquals(88L, dto.getPackageId());
        assertEquals("2", dto.getFloor());
        assertEquals("201", dto.getRoom());
        assertFalse(dto.getIncludeOutstandingBalance());
    }

    // ============================================================
    // 3) Builder tests
    // ============================================================
    @Test
    void testBuilder() {
        LocalDateTime dueDate = LocalDateTime.now();

        CreateInvoiceRequest dto = CreateInvoiceRequest.builder()
                .contractId(5L)
                .dueDate(dueDate)
                .subTotal(5000)
                .invoiceStatus(0)
                .room("101")
                .floor("1")
                .includeOutstandingBalance(true)
                .build();

        assertEquals(5L, dto.getContractId());
        assertEquals(dueDate, dto.getDueDate());
        assertEquals(5000, dto.getSubTotal());
        assertEquals(0, dto.getInvoiceStatus());
        assertEquals("101", dto.getRoom());
        assertEquals("1", dto.getFloor());
        assertTrue(dto.getIncludeOutstandingBalance());
    }

    // ============================================================
    // 4) Null safety
    // ============================================================
    @Test
    void testNullValues() {
        CreateInvoiceRequest dto = new CreateInvoiceRequest();

        dto.setNotes(null);
        dto.setCreateDate(null);
        dto.setRoom(null);
        dto.setFloor(null);
        dto.setIncludeOutstandingBalance(null);

        assertNull(dto.getNotes());
        assertNull(dto.getCreateDate());
        assertNull(dto.getRoom());
        assertNull(dto.getFloor());
        assertNull(dto.getIncludeOutstandingBalance());
    }

    // ============================================================
    // 5) Test alias: elecUnit should be independent
    // ============================================================
    @Test
    void testElecUnitAlias() {
        CreateInvoiceRequest dto = new CreateInvoiceRequest();

        dto.setElecUnit(9);
        assertEquals(9, dto.getElecUnit());
    }

    // ============================================================
    // 6) toString() not null
    // ============================================================
    @Test
    void testToString() {
        CreateInvoiceRequest dto = new CreateInvoiceRequest();
        assertNotNull(dto.toString());
    }
}
