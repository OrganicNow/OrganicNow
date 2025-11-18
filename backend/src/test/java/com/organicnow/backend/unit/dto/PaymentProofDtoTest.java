package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.PaymentProofDto;
import com.organicnow.backend.model.PaymentProof;
import com.organicnow.backend.model.PaymentRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentProofDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        return m;
    }

    @Test
    void testGetterSetter() {
        PaymentProofDto dto = new PaymentProofDto();

        dto.setId(10L);
        dto.setPaymentRecordId(20L);
        dto.setFileName("receipt.jpg");
        dto.setFilePath("/uploads/receipt.jpg");
        dto.setFileSize(5000L);
        dto.setContentType("image/jpeg");
        dto.setProofType(PaymentProof.ProofType.BANK_SLIP);
        dto.setProofTypeDisplay("Transfer Slip");
        dto.setDescription("Bank transfer receipt");
        dto.setUploadedBy("admin");
        LocalDateTime now = LocalDateTime.now();
        dto.setUploadedAt(now);

        assertEquals(10L, dto.getId());
        assertEquals(20L, dto.getPaymentRecordId());
        assertEquals("receipt.jpg", dto.getFileName());
        assertEquals("/uploads/receipt.jpg", dto.getFilePath());
        assertEquals(5000L, dto.getFileSize());
        assertEquals("image/jpeg", dto.getContentType());
        assertEquals(PaymentProof.ProofType.BANK_SLIP, dto.getProofType());
        assertEquals("Transfer Slip", dto.getProofTypeDisplay());
        assertEquals("Bank transfer receipt", dto.getDescription());
        assertEquals("admin", dto.getUploadedBy());
        assertEquals(now, dto.getUploadedAt());
    }

    @Test
    void testDuplicatedFilenameHelpers() {
        PaymentProofDto dto = PaymentProofDto.builder()
                .fileName("bill.png")
                .contentType("image/png")
                .build();

        assertEquals("bill.png", dto.getOriginalFilename());
        assertEquals("bill.png", dto.getOriginalFileName());
        assertEquals("image/png", dto.getFileType());
    }

    @Test
    void testFromEntity() {
        // Mock PaymentRecord
        PaymentRecord record = mock(PaymentRecord.class);
        when(record.getId()).thenReturn(200L);

        // Mock PaymentProof
        PaymentProof entity = mock(PaymentProof.class);
        when(entity.getId()).thenReturn(100L);
        when(entity.getPaymentRecord()).thenReturn(record);
        when(entity.getFileName()).thenReturn("proof.pdf");
        when(entity.getFilePath()).thenReturn("/files/proof.pdf");
        when(entity.getFileSize()).thenReturn(12345L);
        when(entity.getContentType()).thenReturn("application/pdf");
        when(entity.getProofType()).thenReturn(PaymentProof.ProofType.RECEIPT);
        when(entity.getDescription()).thenReturn("Official receipt");
        when(entity.getUploadedBy()).thenReturn("staff");
        LocalDateTime date = LocalDateTime.now();
        when(entity.getUploadedAt()).thenReturn(date);

        // Convert
        PaymentProofDto dto = PaymentProofDto.fromEntity(entity);

        assertEquals(100L, dto.getId());
        assertEquals(200L, dto.getPaymentRecordId());
        assertEquals("proof.pdf", dto.getFileName());
        assertEquals("/files/proof.pdf", dto.getFilePath());
        assertEquals(12345L, dto.getFileSize());
        assertEquals("application/pdf", dto.getContentType());
        assertEquals(PaymentProof.ProofType.RECEIPT, dto.getProofType());
        assertEquals("ใบเสร็จรับเงิน", dto.getProofTypeDisplay());
        assertEquals("Official receipt", dto.getDescription());
        assertEquals("staff", dto.getUploadedBy());
        assertEquals(date, dto.getUploadedAt());
    }

    @Test
    void testJsonSerialization() throws Exception {
        PaymentProofDto dto1 = PaymentProofDto.builder()
                .id(1L)
                .paymentRecordId(10L)
                .fileName("test.png")
                .filePath("/files/test.png")
                .fileSize(2048L)
                .contentType("image/png")
                .proofType(PaymentProof.ProofType.RECEIPT)
                .proofTypeDisplay(PaymentProof.ProofType.RECEIPT.getDisplayName()) // = "ใบเสร็จรับเงิน"
                .description("desc")
                .uploadedBy("admin")
                .uploadedAt(LocalDateTime.of(2025, 1, 1, 12, 30))
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String json = mapper.writeValueAsString(dto1);
        PaymentProofDto dto2 = mapper.readValue(json, PaymentProofDto.class);

        assertEquals(dto1.getId(), dto2.getId());
        assertEquals(dto1.getProofType(), dto2.getProofType());
        assertEquals(dto1.getProofTypeDisplay(), dto2.getProofTypeDisplay());
        assertEquals(dto1.getUploadedAt(), dto2.getUploadedAt());
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
              "id": 50,
              "paymentRecordId": 99,
              "fileName": "abc.jpg",
              "filePath": "/images/abc.jpg",
              "fileSize": 4000,
              "contentType": "image/jpeg",
              "proofType": "RECEIPT",
              "proofTypeDisplay": "Receipt",
              "description": "sample",
              "uploadedBy": "tester",
              "uploadedAt": "2025-02-01T12:30:00"
            }
            """;

        PaymentProofDto dto = mapper().readValue(json, PaymentProofDto.class);

        assertEquals(50L, dto.getId());
        assertEquals(99L, dto.getPaymentRecordId());
        assertEquals("abc.jpg", dto.getFileName());
        assertEquals("/images/abc.jpg", dto.getFilePath());
        assertEquals(4000L, dto.getFileSize());
        assertEquals("image/jpeg", dto.getContentType());
        assertEquals(PaymentProof.ProofType.RECEIPT, dto.getProofType());
        assertEquals("Receipt", dto.getProofTypeDisplay());
        assertEquals("sample", dto.getDescription());
        assertEquals("tester", dto.getUploadedBy());
        assertEquals(LocalDateTime.of(2025, 2, 1, 12, 30), dto.getUploadedAt());
    }
}
