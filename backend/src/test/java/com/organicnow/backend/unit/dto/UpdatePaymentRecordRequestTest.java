package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.UpdatePaymentRecordRequest;
import com.organicnow.backend.model.PaymentRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UpdatePaymentRecordRequestTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void testGetterAndSetter() {
        UpdatePaymentRecordRequest dto = new UpdatePaymentRecordRequest();

        LocalDateTime now = LocalDateTime.now();

        dto.setPaymentAmount(new BigDecimal("1500.50"));
        dto.setPaymentMethod(PaymentRecord.PaymentMethod.BANK_TRANSFER);
        dto.setPaymentStatus(PaymentRecord.PaymentStatus.CONFIRMED);
        dto.setPaymentDate(now);
        dto.setTransactionReference("TX-0001");
        dto.setNotes("Paid fully");

        assertEquals(new BigDecimal("1500.50"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.BANK_TRANSFER, dto.getPaymentMethod());
        assertEquals(PaymentRecord.PaymentStatus.CONFIRMED, dto.getPaymentStatus());
        assertEquals(now, dto.getPaymentDate());
        assertEquals("TX-0001", dto.getTransactionReference());
        assertEquals("Paid fully", dto.getNotes());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        UpdatePaymentRecordRequest dto = new UpdatePaymentRecordRequest(
                new BigDecimal("2200"),
                PaymentRecord.PaymentMethod.CASH,
                PaymentRecord.PaymentStatus.PENDING,
                now,
                "REF123",
                "Waiting confirmation"
        );

        assertEquals(new BigDecimal("2200"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.CASH, dto.getPaymentMethod());
        assertEquals(PaymentRecord.PaymentStatus.PENDING, dto.getPaymentStatus());
        assertEquals(now, dto.getPaymentDate());
        assertEquals("REF123", dto.getTransactionReference());
        assertEquals("Waiting confirmation", dto.getNotes());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();

        UpdatePaymentRecordRequest dto = UpdatePaymentRecordRequest.builder()
                .paymentAmount(new BigDecimal("999.99"))
                .paymentMethod(PaymentRecord.PaymentMethod.QR_CODE)
                .paymentStatus(PaymentRecord.PaymentStatus.CONFIRMED)
                .paymentDate(now)
                .transactionReference("QR-555")
                .notes("Auto processed")
                .build();

        assertEquals(new BigDecimal("999.99"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.QR_CODE, dto.getPaymentMethod());
        assertEquals(PaymentRecord.PaymentStatus.CONFIRMED, dto.getPaymentStatus());
        assertEquals(now, dto.getPaymentDate());
        assertEquals("QR-555", dto.getTransactionReference());
        assertEquals("Auto processed", dto.getNotes());
    }

    @Test
    void testJsonSerialization() throws Exception {
        UpdatePaymentRecordRequest dto = UpdatePaymentRecordRequest.builder()
                .paymentAmount(new BigDecimal("3000"))
                .paymentMethod(PaymentRecord.PaymentMethod.MOBILE_BANKING)
                .paymentStatus(PaymentRecord.PaymentStatus.PENDING)
                .transactionReference("MB123")
                .build();

        String json = mapper.writeValueAsString(dto);

        assertNotNull(json);
        assertTrue(json.contains("3000"));
        assertTrue(json.contains("MOBILE_BANKING"));
        assertTrue(json.contains("PENDING"));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
              "paymentAmount": 1800,
              "paymentMethod": "CREDIT_CARD",
              "paymentStatus": "REJECTED",
              "transactionReference": "CC987"
            }
        """;

        UpdatePaymentRecordRequest dto = mapper.readValue(json, UpdatePaymentRecordRequest.class);

        assertEquals(new BigDecimal("1800"), dto.getPaymentAmount());
        assertEquals(PaymentRecord.PaymentMethod.CREDIT_CARD, dto.getPaymentMethod());
        assertEquals(PaymentRecord.PaymentStatus.REJECTED, dto.getPaymentStatus());
        assertEquals("CC987", dto.getTransactionReference());
    }

    @Test
    void testAllowNullFields() {
        UpdatePaymentRecordRequest dto = new UpdatePaymentRecordRequest();

        assertNull(dto.getPaymentAmount());
        assertNull(dto.getPaymentMethod());
        assertNull(dto.getPaymentStatus());
        assertNull(dto.getPaymentDate());
        assertNull(dto.getTransactionReference());
        assertNull(dto.getNotes());
    }

    @Test
    void testToStringNotEmpty() {
        UpdatePaymentRecordRequest dto = new UpdatePaymentRecordRequest();
        assertNotNull(dto.toString());
        assertFalse(dto.toString().isBlank());
    }
}
