package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.TenantDetailDto;
import com.organicnow.backend.dto.TenantDetailDto.InvoiceDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TenantDetailDtoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testGetterSetter() {
        TenantDetailDto dto = new TenantDetailDto();

        dto.setContractId(1L);
        dto.setFirstName("Alice");
        dto.setLastName("Smith");
        dto.setEmail("alice@example.com");
        dto.setPhoneNumber("0999999999");
        dto.setNationalId("1234567890123");
        dto.setFloor(5);
        dto.setRoom("A501");
        dto.setContractTypeId(10L);
        dto.setPackageName("Premium");
        dto.setPackagePrice(new BigDecimal("5999.99"));
        dto.setSignDate(LocalDateTime.now());
        dto.setStartDate(LocalDateTime.now().minusDays(1));
        dto.setEndDate(LocalDateTime.now().plusMonths(1));
        dto.setStatus(1);
        dto.setDeposit(new BigDecimal("5000"));
        dto.setRentAmountSnapshot(new BigDecimal("12000"));

        assertEquals(1L, dto.getContractId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("0999999999", dto.getPhoneNumber());
        assertEquals("1234567890123", dto.getNationalId());
        assertEquals(5, dto.getFloor());
        assertEquals("A501", dto.getRoom());
        assertEquals(10L, dto.getContractTypeId());
        assertEquals("Premium", dto.getPackageName());
        assertEquals(new BigDecimal("5999.99"), dto.getPackagePrice());
    }


    @Test
    void testBuilderPattern() {
        LocalDateTime now = LocalDateTime.now();

        TenantDetailDto dto = TenantDetailDto.builder()
                .contractId(2L)
                .firstName("Bob")
                .lastName("Taylor")
                .email("bob@example.com")
                .phoneNumber("0888888888")
                .nationalId("9876543210000")
                .floor(3)
                .room("B302")
                .contractTypeId(20L)
                .packageName("Standard")
                .packagePrice(new BigDecimal("2999"))
                .signDate(now)
                .startDate(now.minusDays(10))
                .endDate(now.plusDays(30))
                .status(0)
                .deposit(new BigDecimal("3000"))
                .rentAmountSnapshot(new BigDecimal("8500"))
                .build();

        assertEquals(2L, dto.getContractId());
        assertEquals("Bob", dto.getFirstName());
        assertEquals("Standard", dto.getPackageName());
        assertEquals("B302", dto.getRoom());
    }


    @Test
    void testInvoiceDtoBuilder() {
        LocalDateTime now = LocalDateTime.now();

        InvoiceDto invoice = InvoiceDto.builder()
                .invoiceId(100L)
                .createDate(now)
                .dueDate(now.plusDays(7))
                .invoiceStatus(1)
                .netAmount(8000)
                .payDate(now.plusDays(1))
                .payMethod(2)
                .penaltyTotal(0)
                .subTotal(8000)
                .build();

        assertEquals(100L, invoice.getInvoiceId());
        assertEquals(8000, invoice.getNetAmount());
        assertEquals(0, invoice.getPenaltyTotal());
    }


    @Test
    void testTenantDetailDtoWithInvoices() {
        InvoiceDto inv = InvoiceDto.builder()
                .invoiceId(1L)
                .invoiceStatus(1)
                .netAmount(5000)
                .build();

        TenantDetailDto dto = new TenantDetailDto();
        dto.setInvoices(List.of(inv));

        assertNotNull(dto.getInvoices());
        assertEquals(1, dto.getInvoices().size());
        assertEquals(5000, dto.getInvoices().get(0).getNetAmount());
    }


    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        TenantDetailDto dto = TenantDetailDto.builder()
                .contractId(99L)
                .firstName("John")
                .lastName("Doe")
                .room("C203")
                .floor(2)
                .packageName("Gold")
                .packagePrice(new BigDecimal("4500"))
                .invoices(List.of(
                        InvoiceDto.builder()
                                .invoiceId(10L)
                                .netAmount(7000)
                                .invoiceStatus(1)
                                .build()
                ))
                .build();

        String json = mapper.writeValueAsString(dto);
        TenantDetailDto read = mapper.readValue(json, TenantDetailDto.class);

        assertEquals(dto.getContractId(), read.getContractId());
        assertEquals(dto.getFirstName(), read.getFirstName());
        assertEquals("C203", read.getRoom());
        assertNotNull(read.getInvoices());
        assertEquals(1, read.getInvoices().size());
        assertEquals(7000, read.getInvoices().get(0).getNetAmount());
    }


    @Test
    void testToStringIsNotEmpty() {
        TenantDetailDto dto = new TenantDetailDto();
        assertNotNull(dto.toString());
        assertFalse(dto.toString().isBlank());
    }

}
