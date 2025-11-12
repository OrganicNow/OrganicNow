package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.PaymentProof;
import com.organicnow.backend.model.PaymentProof.ProofType;
import com.organicnow.backend.model.PaymentRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentProofTest {

    @Test
    void testGetterAndSetter() {
        PaymentRecord record = new PaymentRecord();
        record.setId(10L);

        PaymentProof proof = new PaymentProof();
        proof.setId(100L);
        proof.setPaymentRecord(record);
        proof.setFileName("receipt.pdf");
        proof.setFilePath("/uploads/payments/receipt.pdf");
        proof.setFileSize(204800L);
        proof.setContentType("application/pdf");
        proof.setProofType(ProofType.RECEIPT);
        proof.setDescription("Payment for April invoice");
        proof.setUploadedBy("admin");
        proof.setUploadedAt(LocalDateTime.of(2025, 11, 12, 14, 30));

        assertEquals(100L, proof.getId());
        assertEquals(10L, proof.getPaymentRecord().getId());
        assertEquals("receipt.pdf", proof.getFileName());
        assertEquals("/uploads/payments/receipt.pdf", proof.getFilePath());
        assertEquals(204800L, proof.getFileSize());
        assertEquals("application/pdf", proof.getContentType());
        assertEquals(ProofType.RECEIPT, proof.getProofType());
        assertEquals("Payment for April invoice", proof.getDescription());
        assertEquals("admin", proof.getUploadedBy());
        assertEquals(LocalDateTime.of(2025, 11, 12, 14, 30), proof.getUploadedAt());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        PaymentRecord record = new PaymentRecord();
        record.setId(20L);

        PaymentProof proof = PaymentProof.builder()
                .id(200L)
                .paymentRecord(record)
                .fileName("slip.png")
                .filePath("/uploads/slips/slip.png")
                .fileSize(102400L)
                .contentType("image/png")
                .proofType(ProofType.BANK_SLIP)
                .description("Bank transfer evidence")
                .uploadedBy("staffA")
                .uploadedAt(LocalDateTime.of(2025, 11, 1, 10, 15))
                .build();

        assertNotNull(proof);
        assertEquals(200L, proof.getId());
        assertEquals(20L, proof.getPaymentRecord().getId());
        assertEquals("slip.png", proof.getFileName());
        assertEquals("image/png", proof.getContentType());
        assertEquals(ProofType.BANK_SLIP, proof.getProofType());
        assertEquals("staffA", proof.getUploadedBy());
        assertEquals(LocalDateTime.of(2025, 11, 1, 10, 15), proof.getUploadedAt());
    }

    @Test
    void testAllArgsConstructor() {
        PaymentRecord record = new PaymentRecord();
        record.setId(30L);

        PaymentProof proof = new PaymentProof(
                300L,
                record,
                "cheque.jpg",
                "/uploads/cheques/cheque.jpg",
                512000L,
                "image/jpeg",
                ProofType.CHEQUE_COPY,
                "Cheque payment for May",
                "staffB",
                LocalDateTime.of(2025, 11, 10, 9, 0)
        );

        assertEquals(300L, proof.getId());
        assertEquals(30L, proof.getPaymentRecord().getId());
        assertEquals("cheque.jpg", proof.getFileName());
        assertEquals("/uploads/cheques/cheque.jpg", proof.getFilePath());
        assertEquals(ProofType.CHEQUE_COPY, proof.getProofType());
        assertEquals("Cheque payment for May", proof.getDescription());
        assertEquals("staffB", proof.getUploadedBy());
    }

    @Test
    void testProofTypeDisplayNames() {
        assertEquals("ใบเสร็จรับเงิน", ProofType.RECEIPT.getDisplayName());
        assertEquals("สลิปการโอนเงิน", ProofType.BANK_SLIP.getDisplayName());
        assertEquals("Statement ธนาคาร", ProofType.BANK_STATEMENT.getDisplayName());
        assertEquals("สำเนาเช็ค", ProofType.CHEQUE_COPY.getDisplayName());
        assertEquals("หลักฐานอื่นๆ", ProofType.OTHER.getDisplayName());
    }

    @Test
    void testToStringNotNull() {
        PaymentProof proof = new PaymentProof();
        proof.setFileName("test.png");
        assertNotNull(proof.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        PaymentProof p1 = new PaymentProof();
        PaymentProof p2 = new PaymentProof();

        assertSame(p1, p1);   // อ้างอิงเดียวกัน
        assertNotSame(p1, p2); // คนละอ้างอิง
    }
}
