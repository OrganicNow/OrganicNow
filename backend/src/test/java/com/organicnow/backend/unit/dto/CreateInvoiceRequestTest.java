//package com.organicnow.backend.unit.dto;
//
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class CreateInvoiceRequestTest {
//
//    @Test
//    void testNoArgsConstructor() {
//        // Act
//        CreateInvoiceRequest request = new CreateInvoiceRequest();
//
//        // Assert
//        assertNull(request.getContractId());
//        assertNull(request.getDueDate());
//        assertNull(request.getSubTotal());
//        assertNull(request.getPenaltyTotal());
//        assertNull(request.getNetAmount());
//        assertNull(request.getNotes());
//        assertNull(request.getRentAmount());
//        assertNull(request.getWaterUnit());
//        assertNull(request.getWaterRate());
//        assertNull(request.getElectricityUnit());
//        assertNull(request.getElectricityRate());
//        assertNull(request.getCreateDate());
//        assertNull(request.getInvoiceStatus());
//        assertNull(request.getWater());
//        assertNull(request.getElectricity());
//        assertNull(request.getElecUnit());
//    }
//
//    @Test
//    void testAllArgsConstructor() {
//        // Arrange
//        Long expectedContractId = 1L;
//        LocalDateTime expectedDueDate = LocalDateTime.of(2025, 2, 28, 12, 0, 0, 0);
//        Integer expectedSubTotal = 1000;
//        Integer expectedPenaltyTotal = 100;
//        Integer expectedNetAmount = 1100;
//        String expectedNotes = "Payment due";
//        Integer expectedRentAmount = 500;
//        Integer expectedWaterUnit = 5;
//        Integer expectedWaterRate = 20;
//        Integer expectedElectricityUnit = 10;
//        Integer expectedElectricityRate = 15;
//        String expectedCreateDate = "2025-02-28"; // ให้เป็น String ตามที่กำหนดใน DTO
//        Integer expectedInvoiceStatus = 1; // Complete
//        Integer expectedWater = 100;
//        Integer expectedElectricity = 150;
//        Integer expectedElecUnit = 10;
//
//        // Act: ใช้ Builder ในการสร้างอ็อบเจ็กต์
//        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
//                .contractId(expectedContractId)
//                .dueDate(expectedDueDate)
//                .subTotal(expectedSubTotal)
//                .penaltyTotal(expectedPenaltyTotal)
//                .netAmount(expectedNetAmount)
//                .notes(expectedNotes)
//                .rentAmount(expectedRentAmount)
//                .waterUnit(expectedWaterUnit)
//                .waterRate(expectedWaterRate)
//                .electricityUnit(expectedElectricityUnit)
//                .electricityRate(expectedElectricityRate)
//                .createDate(expectedCreateDate) // ใช้เป็น String ตามที่กำหนดใน DTO
//                .invoiceStatus(expectedInvoiceStatus)
//                .water(expectedWater)
//                .electricity(expectedElectricity)
//                .elecUnit(expectedElecUnit)
//                .build();
//
//        // Assert
//        assertEquals(expectedContractId, request.getContractId());
//        assertEquals(expectedDueDate, request.getDueDate());
//        assertEquals(expectedSubTotal, request.getSubTotal());
//        assertEquals(expectedPenaltyTotal, request.getPenaltyTotal());
//        assertEquals(expectedNetAmount, request.getNetAmount());
//        assertEquals(expectedNotes, request.getNotes());
//        assertEquals(expectedRentAmount, request.getRentAmount());
//        assertEquals(expectedWaterUnit, request.getWaterUnit());
//        assertEquals(expectedWaterRate, request.getWaterRate());
//        assertEquals(expectedElectricityUnit, request.getElectricityUnit());
//        assertEquals(expectedElectricityRate, request.getElectricityRate());
//        assertEquals(expectedCreateDate, request.getCreateDate());
//        assertEquals(expectedInvoiceStatus, request.getInvoiceStatus());
//        assertEquals(expectedWater, request.getWater());
//        assertEquals(expectedElectricity, request.getElectricity());
//        assertEquals(expectedElecUnit, request.getElecUnit());
//    }
//
//
//
//    @Test
//    void testBuilder() {
//        // Arrange
//        Long expectedContractId = 2L;
//        LocalDateTime expectedDueDate = LocalDateTime.of(2025, 5, 15, 15, 0, 0, 0);
//        Integer expectedSubTotal = 2000;
//        Integer expectedPenaltyTotal = 200;
//        Integer expectedNetAmount = 2200;
//        String expectedNotes = "Late payment";
//        Integer expectedRentAmount = 1000;
//        Integer expectedWaterUnit = 10;
//        Integer expectedWaterRate = 30;
//        Integer expectedElectricityUnit = 15;
//        Integer expectedElectricityRate = 20;
//        String expectedCreateDate = "2025-05-15";
//        Integer expectedInvoiceStatus = 0; // Incomplete
//        Integer expectedWater = 200;
//        Integer expectedElectricity = 300;
//        Integer expectedElecUnit = 15;
//
//        // Act
//        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
//                .contractId(expectedContractId)
//                .dueDate(expectedDueDate)
//                .subTotal(expectedSubTotal)
//                .penaltyTotal(expectedPenaltyTotal)
//                .netAmount(expectedNetAmount)
//                .notes(expectedNotes)
//                .rentAmount(expectedRentAmount)
//                .waterUnit(expectedWaterUnit)
//                .waterRate(expectedWaterRate)
//                .electricityUnit(expectedElectricityUnit)
//                .electricityRate(expectedElectricityRate)
//                .createDate(expectedCreateDate)
//                .invoiceStatus(expectedInvoiceStatus)
//                .water(expectedWater)
//                .electricity(expectedElectricity)
//                .elecUnit(expectedElecUnit)
//                .build();
//
//        // Assert
//        assertEquals(expectedContractId, request.getContractId());
//        assertEquals(expectedDueDate, request.getDueDate());
//        assertEquals(expectedSubTotal, request.getSubTotal());
//        assertEquals(expectedPenaltyTotal, request.getPenaltyTotal());
//        assertEquals(expectedNetAmount, request.getNetAmount());
//        assertEquals(expectedNotes, request.getNotes());
//        assertEquals(expectedRentAmount, request.getRentAmount());
//        assertEquals(expectedWaterUnit, request.getWaterUnit());
//        assertEquals(expectedWaterRate, request.getWaterRate());
//        assertEquals(expectedElectricityUnit, request.getElectricityUnit());
//        assertEquals(expectedElectricityRate, request.getElectricityRate());
//        assertEquals(expectedCreateDate, request.getCreateDate());
//        assertEquals(expectedInvoiceStatus, request.getInvoiceStatus());
//        assertEquals(expectedWater, request.getWater());
//        assertEquals(expectedElectricity, request.getElectricity());
//        assertEquals(expectedElecUnit, request.getElecUnit());
//    }
//
//    @Test
//    void testSettersAndGetters() {
//        // Arrange
//        CreateInvoiceRequest request = new CreateInvoiceRequest();
//        Long expectedContractId = 3L;
//        String expectedNotes = "Urgent payment";
//
//        // Act
//        request.setContractId(expectedContractId);
//        request.setNotes(expectedNotes);
//
//        // Assert
//        assertEquals(expectedContractId, request.getContractId());
//        assertEquals(expectedNotes, request.getNotes());
//    }
//}
