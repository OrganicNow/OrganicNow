package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.TenantDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TenantDtoTest {

    // ใช้กับ test JSON (รองรับ LocalDateTime ด้วย)
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void testGetterAndSetter() {
        TenantDto dto = new TenantDto();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(30);

        dto.setContractId(1L);
        dto.setFirstName("Alice");
        dto.setLastName("Smith");
        dto.setPhoneNumber("0999999999");
        dto.setEmail("alice@example.com");
        dto.setNationalId("1234567890123");

        dto.setRoom("A101");
        dto.setFloor(1);
        dto.setRoomId(10L);

        dto.setPackageId(5L);
        dto.setContractTypeId(2L);
        dto.setContractName("Standard Contract");

        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setSignDate(now);

        dto.setDeposit(new BigDecimal("5000.00"));
        dto.setRentAmountSnapshot(new BigDecimal("12000.50"));

        dto.setStatus(1);
        dto.setHasSignedPdf(true);

        assertEquals(1L, dto.getContractId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("0999999999", dto.getPhoneNumber());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("1234567890123", dto.getNationalId());

        assertEquals("A101", dto.getRoom());
        assertEquals(1, dto.getFloor());
        assertEquals(10L, dto.getRoomId());

        assertEquals(5L, dto.getPackageId());
        assertEquals(2L, dto.getContractTypeId());
        assertEquals("Standard Contract", dto.getContractName());

        assertEquals(start, dto.getStartDate());
        assertEquals(end, dto.getEndDate());
        assertEquals(now, dto.getSignDate());

        assertEquals(new BigDecimal("5000.00"), dto.getDeposit());
        assertEquals(new BigDecimal("12000.50"), dto.getRentAmountSnapshot());

        assertEquals(1, dto.getStatus());
        assertTrue(dto.isHasSignedPdf());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        LocalDateTime now = LocalDateTime.now();

        TenantDto dto = TenantDto.builder()
                .contractId(2L)
                .firstName("Bob")
                .lastName("Taylor")
                .phoneNumber("0888888888")
                .email("bob@example.com")
                .nationalId("9876543210000")
                .room("B502")
                .floor(5)
                .roomId(50L)
                .packageId(7L)
                .contractTypeId(3L)
                .contractName("Premium Contract")
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(60))
                .signDate(now.minusDays(7))
                .deposit(new BigDecimal("8000"))
                .rentAmountSnapshot(new BigDecimal("15000"))
                .status(1)
                .hasSignedPdf(false)
                .build();

        assertEquals(2L, dto.getContractId());
        assertEquals("Bob", dto.getFirstName());
        assertEquals("Taylor", dto.getLastName());
        assertEquals("B502", dto.getRoom());
        assertEquals(5, dto.getFloor());
        assertEquals(50L, dto.getRoomId());
        assertEquals(7L, dto.getPackageId());
        assertEquals(3L, dto.getContractTypeId());
        assertEquals("Premium Contract", dto.getContractName());
        assertEquals(new BigDecimal("8000"), dto.getDeposit());
        assertEquals(new BigDecimal("15000"), dto.getRentAmountSnapshot());
        assertEquals(1, dto.getStatus());
        assertFalse(dto.isHasSignedPdf());
    }

    @Test
    void testJpqlConstructorMappingAndDefaultHasSignedPdf() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(28);

        TenantDto dto = new TenantDto(
                100L,                  // contractId
                "Charlie",             // firstName
                "Brown",               // lastName
                3,                     // floor
                "C301",                // room
                30L,                   // roomId
                9L,                    // packageId
                4L,                    // contractTypeId
                "Basic Contract",      // contractName
                start,                 // startDate
                end,                   // endDate
                "0777777777",          // phoneNumber
                "charlie@example.com", // email
                "1112223334445",       // nationalId
                0,                     // status
                new BigDecimal("9000") // rentAmountSnapshot
        );

        assertEquals(100L, dto.getContractId());
        assertEquals("Charlie", dto.getFirstName());
        assertEquals("Brown", dto.getLastName());
        assertEquals(3, dto.getFloor());
        assertEquals("C301", dto.getRoom());
        assertEquals(30L, dto.getRoomId());
        assertEquals(9L, dto.getPackageId());
        assertEquals(4L, dto.getContractTypeId());
        assertEquals("Basic Contract", dto.getContractName());
        assertEquals(start, dto.getStartDate());
        assertEquals(end, dto.getEndDate());
        assertEquals("0777777777", dto.getPhoneNumber());
        assertEquals("charlie@example.com", dto.getEmail());
        assertEquals("1112223334445", dto.getNationalId());
        assertEquals(0, dto.getStatus());
        assertEquals(new BigDecimal("9000"), dto.getRentAmountSnapshot());

        // default จาก JPQL constructor
        assertFalse(dto.isHasSignedPdf());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        TenantDto original = TenantDto.builder()
                .contractId(200L)
                .firstName("David")
                .lastName("Lee")
                .phoneNumber("0666666666")
                .email("david@example.com")
                .nationalId("5556667778889")
                .room("D204")
                .floor(2)
                .roomId(204L)
                .packageId(11L)
                .contractTypeId(6L)
                .contractName("Gold Contract")
                .startDate(now.minusDays(3))
                .endDate(now.plusDays(90))
                .signDate(now.minusDays(5))
                .deposit(new BigDecimal("10000"))
                .rentAmountSnapshot(new BigDecimal("20000"))
                .status(1)
                .hasSignedPdf(true)
                .build();

        String json = mapper.writeValueAsString(original);
        TenantDto read = mapper.readValue(json, TenantDto.class);

        assertEquals(original.getContractId(), read.getContractId());
        assertEquals(original.getFirstName(), read.getFirstName());
        assertEquals(original.getLastName(), read.getLastName());
        assertEquals(original.getRoom(), read.getRoom());
        assertEquals(original.getFloor(), read.getFloor());
        assertEquals(original.getRoomId(), read.getRoomId());
        assertEquals(original.getPackageId(), read.getPackageId());
        assertEquals(original.getContractTypeId(), read.getContractTypeId());
        assertEquals(original.getContractName(), read.getContractName());
        assertEquals(original.getStatus(), read.getStatus());
        assertEquals(original.getDeposit(), read.getDeposit());
        assertEquals(original.getRentAmountSnapshot(), read.getRentAmountSnapshot());
        assertEquals(original.isHasSignedPdf(), read.isHasSignedPdf());
    }

    @Test
    void testToStringNotBlank() {
        TenantDto dto = new TenantDto();
        String str = dto.toString();
        assertNotNull(str);
        assertFalse(str.isBlank());
    }
}
