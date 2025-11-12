package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Invoice;
import com.organicnow.backend.model.PaymentRecord;
import com.organicnow.backend.model.PaymentRecord.PaymentMethod;
import com.organicnow.backend.model.PaymentRecord.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRecordTest {

    @Test
    void testGetterAndSetter() {
        Invoice invoice = new Invoice();
        invoice.setId(100L);

        LocalDateTime payDate = LocalDateTime.of(2025, 11, 12, 14, 30);
        LocalDateTime createdAt = LocalDateTime.of(2025, 11, 12, 15, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 11, 13, 10, 0);

        PaymentRecord record = new PaymentRecord();
        record.setId(1L);
        record.setInvoice(invoice);
        record.setPaymentAmount(BigDecimal.valueOf(9999.99));
        record.setPaymentMethod(PaymentMethod.QR_CODE);
        record.setPaymentStatus(PaymentStatus.CONFIRMED);
        record.setPaymentDate(payDate);
        record.setTransactionReference("TXN123456789");
        record.setNotes("Paid successfully via QR");
        record.setRecordedBy("staffA");
        record.setCreatedAt(createdAt);
        record.setUpdatedAt(updatedAt);

        assertEquals(1L, record.getId());
        assertEquals(100L, record.getInvoice().getId());
        assertEquals(BigDecimal.valueOf(9999.99), record.getPaymentAmount());
        assertEquals(PaymentMethod.QR_CODE, record.getPaymentMethod());
        assertEquals(PaymentStatus.CONFIRMED, record.getPaymentStatus());
        assertEquals(payDate, record.getPaymentDate());
        assertEquals("TXN123456789", record.getTransactionReference());
        assertEquals("Paid successfully via QR", record.getNotes());
        assertEquals("staffA", record.getRecordedBy());
        assertEquals(createdAt, record.getCreatedAt());
        assertEquals(updatedAt, record.getUpdatedAt());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Invoice invoice = new Invoice();
        invoice.setId(200L);

        LocalDateTime payDate = LocalDateTime.of(2025, 10, 10, 9, 0);

        PaymentRecord record = PaymentRecord.builder()
                .id(2L)
                .invoice(invoice)
                .paymentAmount(BigDecimal.valueOf(1500.50))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(payDate)
                .transactionReference("BANK-TX-001")
                .notes("Waiting confirmation")
                .recordedBy("adminUser")
                .createdAt(LocalDateTime.of(2025, 10, 10, 9, 5))
                .updatedAt(LocalDateTime.of(2025, 10, 10, 9, 10))
                .build();

        assertNotNull(record);
        assertEquals(2L, record.getId());
        assertEquals(200L, record.getInvoice().getId());
        assertEquals(BigDecimal.valueOf(1500.50), record.getPaymentAmount());
        assertEquals(PaymentMethod.BANK_TRANSFER, record.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, record.getPaymentStatus());
        assertEquals(payDate, record.getPaymentDate());
        assertEquals("BANK-TX-001", record.getTransactionReference());
        assertEquals("Waiting confirmation", record.getNotes());
        assertEquals("adminUser", record.getRecordedBy());
    }

    @Test
    void testAllArgsConstructor() {
        Invoice invoice = new Invoice();
        invoice.setId(300L);

        LocalDateTime payDate = LocalDateTime.of(2025, 11, 1, 10, 0);

        PaymentRecord record = new PaymentRecord(
                3L,
                invoice,
                BigDecimal.valueOf(800.00),
                PaymentMethod.CASH,
                PaymentStatus.REJECTED,
                payDate,
                "TX-REJ-0001",
                "Invalid payment details",
                "staffB",
                LocalDateTime.of(2025, 11, 1, 10, 5),
                LocalDateTime.of(2025, 11, 1, 10, 10)
        );

        assertEquals(3L, record.getId());
        assertEquals(300L, record.getInvoice().getId());
        assertEquals(BigDecimal.valueOf(800.00), record.getPaymentAmount());
        assertEquals(PaymentMethod.CASH, record.getPaymentMethod());
        assertEquals(PaymentStatus.REJECTED, record.getPaymentStatus());
        assertEquals("TX-REJ-0001", record.getTransactionReference());
        assertEquals("Invalid payment details", record.getNotes());
        assertEquals("staffB", record.getRecordedBy());
    }

    @Test
    void testPaymentMethodDisplayNames() {
        assertEquals("เงินสด", PaymentMethod.CASH.getDisplayName());
        assertEquals("โอนเงินผ่านธนาคาร", PaymentMethod.BANK_TRANSFER.getDisplayName());
        assertEquals("Mobile Banking", PaymentMethod.MOBILE_BANKING.getDisplayName());
        assertEquals("เช็ค", PaymentMethod.CHEQUE.getDisplayName());
        assertEquals("บัตรเครดิต", PaymentMethod.CREDIT_CARD.getDisplayName());
        assertEquals("QR Code Payment", PaymentMethod.QR_CODE.getDisplayName());
        assertEquals("อื่นๆ", PaymentMethod.OTHER.getDisplayName());
    }

    @Test
    void testPaymentStatusDisplayNames() {
        assertEquals("รอการยืนยัน", PaymentStatus.PENDING.getDisplayName());
        assertEquals("ยืนยันแล้ว", PaymentStatus.CONFIRMED.getDisplayName());
        assertEquals("ปฏิเสธ", PaymentStatus.REJECTED.getDisplayName());
        assertEquals("ยกเลิก", PaymentStatus.CANCELLED.getDisplayName());
    }

    @Test
    void testToStringNotNull() {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionReference("TX1234");
        assertNotNull(record.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        PaymentRecord p1 = new PaymentRecord();
        PaymentRecord p2 = new PaymentRecord();

        assertSame(p1, p1);
        assertNotSame(p1, p2);
    }
}
