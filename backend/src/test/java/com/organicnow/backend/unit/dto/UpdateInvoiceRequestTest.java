package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.UpdateInvoiceRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UpdateInvoiceRequestTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void testGetterAndSetter() {
        UpdateInvoiceRequest dto = new UpdateInvoiceRequest();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(5);
        LocalDateTime pay = now.plusDays(2);
        LocalDateTime penaltyDate = now.plusDays(1);

        dto.setCreateDate(now);
        dto.setDueDate(due);
        dto.setInvoiceStatus(1);
        dto.setPayDate(pay);
        dto.setPayMethod(2);
        dto.setSubTotal(1000);
        dto.setPenaltyTotal(50);
        dto.setNetAmount(1050);
        dto.setPenaltyAppliedAt(penaltyDate);
        dto.setNotes("Paid on time");

        dto.setWaterUnit(12);
        dto.setElectricityUnit(45);
        dto.setWaterRate(20.5);
        dto.setElectricityRate(5.75);

        assertEquals(now, dto.getCreateDate());
        assertEquals(due, dto.getDueDate());
        assertEquals(1, dto.getInvoiceStatus());
        assertEquals(pay, dto.getPayDate());
        assertEquals(2, dto.getPayMethod());
        assertEquals(1000, dto.getSubTotal());
        assertEquals(50, dto.getPenaltyTotal());
        assertEquals(1050, dto.getNetAmount());
        assertEquals(penaltyDate, dto.getPenaltyAppliedAt());
        assertEquals("Paid on time", dto.getNotes());

        assertEquals(12, dto.getWaterUnit());
        assertEquals(45, dto.getElectricityUnit());
        assertEquals(20.5, dto.getWaterRate());
        assertEquals(5.75, dto.getElectricityRate());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(3);

        UpdateInvoiceRequest dto = new UpdateInvoiceRequest(
                now, due, 0, null, 1, 500, 0, 500,
                null, "note-test", 5, 10, 15.0, 7.5
        );

        assertEquals(now, dto.getCreateDate());
        assertEquals(due, dto.getDueDate());
        assertEquals(0, dto.getInvoiceStatus());
        assertEquals(1, dto.getPayMethod());
        assertEquals(500, dto.getSubTotal());
        assertEquals(10, dto.getElectricityUnit());
        assertEquals(15.0, dto.getWaterRate());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();

        UpdateInvoiceRequest dto = UpdateInvoiceRequest.builder()
                .createDate(now)
                .invoiceStatus(1)
                .subTotal(800)
                .netAmount(900)
                .waterUnit(10)
                .electricityUnit(30)
                .waterRate(18.5)
                .electricityRate(6.75)
                .notes("builder test")
                .build();

        assertEquals(now, dto.getCreateDate());
        assertEquals(1, dto.getInvoiceStatus());
        assertEquals(800, dto.getSubTotal());
        assertEquals(900, dto.getNetAmount());
        assertEquals(10, dto.getWaterUnit());
        assertEquals(30, dto.getElectricityUnit());
        assertEquals(18.5, dto.getWaterRate());
        assertEquals(6.75, dto.getElectricityRate());
        assertEquals("builder test", dto.getNotes());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        UpdateInvoiceRequest original = UpdateInvoiceRequest.builder()
                .invoiceStatus(1)
                .subTotal(600)
                .netAmount(650)
                .waterUnit(5)
                .electricityUnit(8)
                .notes("json test")
                .build();

        String json = mapper.writeValueAsString(original);
        UpdateInvoiceRequest read = mapper.readValue(json, UpdateInvoiceRequest.class);

        assertEquals(original.getInvoiceStatus(), read.getInvoiceStatus());
        assertEquals(original.getSubTotal(), read.getSubTotal());
        assertEquals(original.getNetAmount(), read.getNetAmount());
        assertEquals(original.getWaterUnit(), read.getWaterUnit());
        assertEquals(original.getElectricityUnit(), read.getElectricityUnit());
        assertEquals(original.getNotes(), read.getNotes());
    }

    @Test
    void testUnknownJsonFieldsIgnored() throws Exception {
        String jsonWithExtra = """
        {
            "invoiceStatus": 1,
            "subTotal": 900,
            "unknownField1": "ABC",
            "waterUnit": 7,
            "unknownField2": 12345
        }
        """;

        UpdateInvoiceRequest dto = mapper.readValue(jsonWithExtra, UpdateInvoiceRequest.class);

        assertEquals(1, dto.getInvoiceStatus());
        assertEquals(900, dto.getSubTotal());
        assertEquals(7, dto.getWaterUnit());
    }

    @Test
    void testToStringNotBlank() {
        UpdateInvoiceRequest dto = new UpdateInvoiceRequest();
        String ts = dto.toString();

        assertNotNull(ts);
        assertFalse(ts.isBlank());
    }
}
