package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.organicnow.backend.dto.PaymentProofDto;
import com.organicnow.backend.dto.PaymentRecordDto;
import com.organicnow.backend.model.Invoice;
import com.organicnow.backend.model.PaymentProof;
import com.organicnow.backend.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentRecordDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    private Invoice createInvoice(Long id) throws Exception {
        Invoice invoice = new Invoice();
        Field idField = Invoice.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(invoice, id);
        return invoice;
    }

    private PaymentRecord createRecord() throws Exception {
        PaymentRecord record = new PaymentRecord();
        record.setId(100L);
        record.setInvoice(createInvoice(500L));
        record.setPaymentAmount(new BigDecimal("3500.75"));
        record.setPaymentMethod(PaymentRecord.PaymentMethod.BANK_TRANSFER);
        record.setPaymentStatus(PaymentRecord.PaymentStatus.CONFIRMED);
        record.setPaymentDate(LocalDateTime.of(2025, 1, 5, 14, 30));
        record.setTransactionReference("TX123");
        record.setNotes("Paid fully");
        record.setRecordedBy("Admin");
        record.setCreatedAt(LocalDateTime.of(2025, 1, 5, 14, 35));
        record.setUpdatedAt(LocalDateTime.of(2025, 1, 5, 14, 40));
        return record;
    }

    // ------------------------------------------------------------------------

    @Test
    void testFromEntity() throws Exception {
        PaymentRecord entity = createRecord();

        PaymentRecordDto dto = PaymentRecordDto.fromEntity(entity);

        assertEquals(100L, dto.getId());
        assertEquals(500L, dto.getInvoiceId());
        assertEquals(new BigDecimal("3500.75"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.BANK_TRANSFER, dto.getPaymentMethod());
        assertEquals("โอนเงินผ่านธนาคาร", dto.getPaymentMethodDisplay());
        assertEquals(PaymentRecord.PaymentStatus.CONFIRMED, dto.getPaymentStatus());
        assertEquals("ยืนยันแล้ว", dto.getPaymentStatusDisplay());
        assertEquals(LocalDateTime.of(2025, 1, 5, 14, 30), dto.getPaymentDate());
        assertEquals("TX123", dto.getTransactionReference());
        assertEquals("Paid fully", dto.getNotes());
        assertEquals("Admin", dto.getRecordedBy());
    }

    // ------------------------------------------------------------------------

    @Test
    void testJsonSerialization() throws Exception {
        PaymentRecordDto dto = PaymentRecordDto.builder()
                .id(1L)
                .invoiceId(50L)
                .paymentAmount(new BigDecimal("1200"))
                .paymentMethod(PaymentRecord.PaymentMethod.CASH)
                .paymentMethodDisplay("เงินสด")
                .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                .paymentStatusDisplay("รอการยืนยัน")
                .paymentDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .transactionReference("TX001")
                .notes("OK")
                .recordedBy("Tester")
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 5))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 11, 0))
                .build();

        String json = mapper().writeValueAsString(dto);

        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"invoiceId\":50"));
        assertTrue(json.contains("\"paymentMethod\":\"CASH\""));
        assertTrue(json.contains("\"paymentStatus\":\"PENDING\""));
        assertTrue(json.contains("2025-01-01T10:00:00"));
    }

    // ------------------------------------------------------------------------

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
                "id": 99,
                "invoiceId": 123,
                "paymentAmount": 5000,
                "paymentMethod": "QR_CODE",
                "paymentStatus": "CONFIRMED",
                "paymentDate": "2025-01-10T12:00:00",
                "transactionReference": "TX777",
                "notes": "Done",
                "recordedBy": "System"
            }
        """;

        PaymentRecordDto dto = mapper().readValue(json, PaymentRecordDto.class);

        assertEquals(99L, dto.getId());
        assertEquals(123L, dto.getInvoiceId());
        assertEquals(new BigDecimal("5000"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.QR_CODE, dto.getPaymentMethod());
        assertEquals(PaymentRecord.PaymentStatus.CONFIRMED, dto.getPaymentStatus());
        assertEquals(LocalDateTime.of(2025, 1, 10, 12, 0), dto.getPaymentDate());
    }

    // ------------------------------------------------------------------------

    @Test
    void testProofListSerialization() throws Exception {

        PaymentProofDto proof = PaymentProofDto.builder()
                .id(10L)
                .paymentRecordId(1L)
                .fileName("slip.jpg")
                .filePath("/uploads/slip.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .proofType(PaymentProof.ProofType.BANK_SLIP)
                .proofTypeDisplay("สลิปการโอนเงิน")
                .description("Uploaded")
                .uploadedBy("User")
                .uploadedAt(LocalDateTime.of(2025,1,1,9,0))
                .build();

        PaymentRecordDto dto = PaymentRecordDto.builder()
                .id(1L)
                .invoiceId(10L)
                .paymentProofs(List.of(proof))
                .build();

        String json = mapper().writeValueAsString(dto);

        assertTrue(json.contains("slip.jpg"));
        assertTrue(json.contains("BANK_SLIP"));

        PaymentRecordDto dto2 = mapper().readValue(json, PaymentRecordDto.class);

        assertEquals(1, dto2.getPaymentProofs().size());
        assertEquals("slip.jpg", dto2.getPaymentProofs().get(0).getFileName());
    }

    // ------------------------------------------------------------------------

    @Test
    void testEnumMapping() {
        assertEquals("เงินสด", PaymentRecord.PaymentMethod.CASH.getDisplayName());
        assertEquals("QR Code Payment", PaymentRecord.PaymentMethod.QR_CODE.getDisplayName());
        assertEquals("ยืนยันแล้ว", PaymentRecord.PaymentStatus.CONFIRMED.getDisplayName());
        assertEquals("ปฏิเสธ", PaymentRecord.PaymentStatus.REJECTED.getDisplayName());
    }

    // ------------------------------------------------------------------------

    @Test
    void testInvoiceIdMappingUsingReflection() throws Exception {
        PaymentRecord record = new PaymentRecord();
        record.setId(900L);

        Invoice inv = createInvoice(888L);
        record.setInvoice(inv);

        // 🔥 เพิ่มค่านี้เพื่อกัน NPE
        record.setPaymentAmount(new BigDecimal("1000"));
        record.setPaymentMethod(PaymentRecord.PaymentMethod.CASH);
        record.setPaymentStatus(PaymentRecord.PaymentStatus.CONFIRMED);
        record.setPaymentDate(LocalDateTime.now());

        PaymentRecordDto dto = PaymentRecordDto.fromEntity(record);

        assertEquals(888L, dto.getInvoiceId());
    }

}
