package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.CreatePaymentRecordRequest;
import com.organicnow.backend.model.PaymentRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreatePaymentRecordRequestTest {

    // ============================================================
    // 1) Default constructor + setters/getters
    // ============================================================
    @Test
    void testDefaultConstructorAndSetters() {
        CreatePaymentRecordRequest dto = new CreatePaymentRecordRequest();

        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("1500.50");

        dto.setInvoiceId(9L);
        dto.setPaymentAmount(amount);
        dto.setPaymentMethod(PaymentRecord.PaymentMethod.CASH);
        dto.setPaymentDate(now);
        dto.setTransactionReference("TXN12345");
        dto.setNotes("Paid in cash at counter");
        dto.setRecordedBy("AdminUser");

        assertEquals(9L, dto.getInvoiceId());
        assertEquals(amount, dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.CASH, dto.getPaymentMethod());
        assertEquals(now, dto.getPaymentDate());
        assertEquals("TXN12345", dto.getTransactionReference());
        assertEquals("Paid in cash at counter", dto.getNotes());
        assertEquals("AdminUser", dto.getRecordedBy());
    }

    // ============================================================
    // 2) AllArgsConstructor
    // ============================================================
    @Test
    void testAllArgsConstructor() {
        LocalDateTime date = LocalDateTime.now();

        CreatePaymentRecordRequest dto = new CreatePaymentRecordRequest(
                5L,
                new BigDecimal("999.99"),
                PaymentRecord.PaymentMethod.BANK_TRANSFER,
                date,
                "REF-777",
                "Monthly payment",
                "SystemUser"
        );

        assertEquals(5L, dto.getInvoiceId());
        assertEquals(new BigDecimal("999.99"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.BANK_TRANSFER, dto.getPaymentMethod());
        assertEquals(date, dto.getPaymentDate());
        assertEquals("REF-777", dto.getTransactionReference());
        assertEquals("Monthly payment", dto.getNotes());
        assertEquals("SystemUser", dto.getRecordedBy());
    }

    // ============================================================
    // 3) Builder pattern
    // ============================================================
    @Test
    void testBuilder() {
        LocalDateTime payDate = LocalDateTime.now();

        CreatePaymentRecordRequest dto = CreatePaymentRecordRequest.builder()
                .invoiceId(33L)
                .paymentAmount(new BigDecimal("2000.00"))
                .paymentMethod(PaymentRecord.PaymentMethod.CASH)   // ← แก้ตรงนี้
                .paymentDate(payDate)
                .transactionReference("QR-8888")
                .notes("QR Payment Success")
                .recordedBy("Staff01")
                .build();

        assertEquals(PaymentRecord.PaymentMethod.CASH, dto.getPaymentMethod()); // ← ตรงนี้ด้วย


        assertEquals(33L, dto.getInvoiceId());
        assertEquals(new BigDecimal("2000.00"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.CASH, dto.getPaymentMethod());
        assertEquals(payDate, dto.getPaymentDate());
        assertEquals("QR-8888", dto.getTransactionReference());
        assertEquals("QR Payment Success", dto.getNotes());
        assertEquals("Staff01", dto.getRecordedBy());
    }

    // ============================================================
    // 4) Null Safety test
    // ============================================================
    @Test
    void testNullValues() {
        CreatePaymentRecordRequest dto = new CreatePaymentRecordRequest();

        dto.setInvoiceId(null);
        dto.setPaymentAmount(null);
        dto.setPaymentMethod(null);
        dto.setPaymentDate(null);
        dto.setTransactionReference(null);
        dto.setNotes(null);
        dto.setRecordedBy(null);

        assertNull(dto.getInvoiceId());
        assertNull(dto.getPaymentAmount());
        assertNull(dto.getPaymentMethod());
        assertNull(dto.getPaymentDate());
        assertNull(dto.getTransactionReference());
        assertNull(dto.getNotes());
        assertNull(dto.getRecordedBy());
    }

    // ============================================================
    // 5) toString() should not be null
    // ============================================================
    @Test
    void testToString() {
        CreatePaymentRecordRequest dto = new CreatePaymentRecordRequest();
        assertNotNull(dto.toString());
    }
}
