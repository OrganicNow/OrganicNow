package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.dto.PaymentRecordDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceDtoTest {

    @Test
    void testInvoiceDtoFieldsAndHelpers() {

        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0);

        InvoiceDto dto = InvoiceDto.builder()
                .id(1L)
                .contractId(20L)
                .contractDetails("Contract A")
                .createDate(now)
                .dueDate(now)
                .invoiceStatus(1)
                .payMethod(2)
                .subTotal(5000)
                .penaltyTotal(300)
                .netAmount(5300)
                .penaltyAppliedAt(now)

                .previousBalance(100)
                .paidAmount(2000)
                .outstandingBalance(3200)
                .hasOutstandingBalance(true)

                .paymentRecords(List.of(new PaymentRecordDto()))
                .totalPaidAmount(new BigDecimal("2000"))
                .totalPendingAmount(new BigDecimal("300"))
                .remainingAmount(new BigDecimal("500"))

                .firstName("John")
                .lastName("Doe")
                .nationalId("1234567890123")
                .phoneNumber("0800000000")
                .email("john@example.com")
                .packageName("Premium")
                .signDate(now)
                .startDate(now)
                .endDate(now)
                .floor(3)
                .room("A301")

                .rent(7000)
                .water(150)
                .waterUnit(10)
                .electricity(250)
                .electricityUnit(40)
                .addonAmount(200)

                .build();

        // ================
        // BASIC FIELD TEST
        // ================
        assertEquals(1L, dto.getId());
        assertEquals(20L, dto.getContractId());
        assertEquals("Contract A", dto.getContractDetails());

        assertEquals(now, dto.getCreateDate());
        assertEquals(now, dto.getDueDate());

        assertEquals(1, dto.getInvoiceStatus());
        assertEquals(2, dto.getPayMethod());

        assertEquals(5000, dto.getSubTotal());
        assertEquals(300, dto.getPenaltyTotal());
        assertEquals(5300, dto.getNetAmount());
        assertEquals(now, dto.getPenaltyAppliedAt());

        assertEquals(100, dto.getPreviousBalance());
        assertEquals(2000, dto.getPaidAmount());
        assertEquals(3200, dto.getOutstandingBalance());
        assertTrue(dto.getHasOutstandingBalance());

        assertNotNull(dto.getPaymentRecords());
        assertEquals(new BigDecimal("2000"), dto.getTotalPaidAmount());
        assertEquals(new BigDecimal("300"), dto.getTotalPendingAmount());
        assertEquals(new BigDecimal("500"), dto.getRemainingAmount());

        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("1234567890123", dto.getNationalId());
        assertEquals("0800000000", dto.getPhoneNumber());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("Premium", dto.getPackageName());

        assertEquals(now, dto.getSignDate());
        assertEquals(now, dto.getStartDate());
        assertEquals(now, dto.getEndDate());

        assertEquals(3, dto.getFloor());
        assertEquals("A301", dto.getRoom());

        assertEquals(7000, dto.getRent());
        assertEquals(150, dto.getWater());
        assertEquals(10, dto.getWaterUnit());
        assertEquals(250, dto.getElectricity());
        assertEquals(40, dto.getElectricityUnit());
        assertEquals(200, dto.getAddonAmount());

        // ==========================
        // HELPER METHOD TEST (LOGIC)
        // ==========================

        // Status Helper
        assertEquals("Complete", dto.getStatusText());
        assertEquals("Complete", dto.getStatus());

        // Pay Method Helper
        assertEquals("โอนเงิน", dto.getPayMethodText());

        // Penalty Helper
        assertEquals(1, dto.getPenalty()); // เพราะ penaltyTotal = 300 (>0)

        // ========================
        // ALIAS TEST (amount/netAmount)
        // ========================
        assertEquals(5300, dto.getAmount());

        dto.setAmount(9999);
        assertEquals(9999, dto.getNetAmount());
        assertEquals(9999, dto.getAmount());

        // =============
        // SET STATUS TEST
        // =============
        dto.setStatus("Cancelled");
        assertEquals(2, dto.getInvoiceStatus());
        assertEquals("Cancelled", dto.getStatus());

        dto.setStatus("Incomplete");
        assertEquals(0, dto.getInvoiceStatus());
        assertEquals("Incomplete", dto.getStatus());
    }

    @Test
    void testPenaltyWhenNull() {
        InvoiceDto dto = new InvoiceDto();
        dto.setPenaltyTotal(null);
        assertEquals(0, dto.getPenalty());
    }

    @Test
    void testStatusWhenNull() {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceStatus(null);
        assertEquals("Unknown", dto.getStatus());
        assertEquals("ไม่ระบุ", dto.getStatusText());
    }

    @Test
    void testPayMethodWhenNull() {
        InvoiceDto dto = new InvoiceDto();
        dto.setPayMethod(null);
        assertEquals("ไม่ระบุ", dto.getPayMethodText());
    }
}
