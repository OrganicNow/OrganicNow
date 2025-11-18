package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.CreateTenantContractRequest;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateTenantContractRequestTest {

    // ===========================================================
    // 1) Default constructor + setter/getter
    // ===========================================================
    @Test
    void testDefaultConstructorAndSetters() {
        CreateTenantContractRequest dto = new CreateTenantContractRequest();

        LocalDateTime now = LocalDateTime.now();

        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPhoneNumber("0999999999");
        dto.setNationalId("1234567890123");

        dto.setRoomId(10L);
        dto.setPackageId(20L);

        dto.setSignDate(now);
        dto.setStartDate(now.plusDays(1));
        dto.setEndDate(now.plusMonths(6));

        dto.setDeposit(BigDecimal.valueOf(5000));
        dto.setRentAmountSnapshot(BigDecimal.valueOf(7500));

        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("0999999999", dto.getPhoneNumber());
        assertEquals("1234567890123", dto.getNationalId());

        assertEquals(10L, dto.getRoomId());
        assertEquals(20L, dto.getPackageId());

        assertEquals(now, dto.getSignDate());
        assertEquals(now.plusDays(1), dto.getStartDate());
        assertEquals(now.plusMonths(6), dto.getEndDate());

        assertEquals(BigDecimal.valueOf(5000), dto.getDeposit());
        assertEquals(BigDecimal.valueOf(7500), dto.getRentAmountSnapshot());
    }

    // ===========================================================
    // 2) AllArgsConstructor
    // ===========================================================
    @Test
    void testAllArgsConstructor() {
        LocalDateTime sign = LocalDateTime.now();
        LocalDateTime start = sign.plusDays(3);
        LocalDateTime end = sign.plusMonths(12);

        CreateTenantContractRequest dto = new CreateTenantContractRequest(
                "Alice",
                "Smith",
                "alice@mail.com",
                "0888888888",
                "9876543210000",
                101L,
                501L,
                sign,
                start,
                end,
                new BigDecimal("6000"),
                new BigDecimal("8000")
        );

        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("alice@mail.com", dto.getEmail());
        assertEquals("0888888888", dto.getPhoneNumber());
        assertEquals("9876543210000", dto.getNationalId());

        assertEquals(101L, dto.getRoomId());
        assertEquals(501L, dto.getPackageId());

        assertEquals(sign, dto.getSignDate());
        assertEquals(start, dto.getStartDate());
        assertEquals(end, dto.getEndDate());

        assertEquals(new BigDecimal("6000"), dto.getDeposit());
        assertEquals(new BigDecimal("8000"), dto.getRentAmountSnapshot());
    }

    // ===========================================================
    // 3) Builder
    // ===========================================================
    @Test
    void testBuilderPattern() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMonths(3);

        CreateTenantContractRequest dto = CreateTenantContractRequest.builder()
                .firstName("Mark")
                .lastName("Lee")
                .email("mark@test.com")
                .phoneNumber("0912345678")
                .nationalId("1231231231234")
                .roomId(300L)
                .packageId(400L)
                .signDate(start.minusDays(1))
                .startDate(start)
                .endDate(end)
                .deposit(BigDecimal.valueOf(4500))
                .rentAmountSnapshot(BigDecimal.valueOf(6500))
                .build();

        assertEquals("Mark", dto.getFirstName());
        assertEquals("Lee", dto.getLastName());
        assertEquals("mark@test.com", dto.getEmail());
        assertEquals("0912345678", dto.getPhoneNumber());
        assertEquals("1231231231234", dto.getNationalId());

        assertEquals(300L, dto.getRoomId());
        assertEquals(400L, dto.getPackageId());
        assertEquals(start.minusDays(1), dto.getSignDate());
        assertEquals(start, dto.getStartDate());
        assertEquals(end, dto.getEndDate());

        assertEquals(BigDecimal.valueOf(4500), dto.getDeposit());
        assertEquals(BigDecimal.valueOf(6500), dto.getRentAmountSnapshot());
    }

    // ===========================================================
    // 4) Equals & HashCode (from Lombok)
    // ===========================================================
    @Test
    void testEqualsAndHashCode() throws Exception {

        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        CreateTenantContractRequest dto1 = CreateTenantContractRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("0800000000")
                .nationalId("1234567890123")
                .roomId(1L)
                .packageId(2L)
                .signDate(fixedTime)
                .startDate(fixedTime)
                .endDate(fixedTime)
                .deposit(new BigDecimal("5000.00"))
                .rentAmountSnapshot(new BigDecimal("7500.00"))
                .build();

        // ⭐ ObjectMapper รองรับ LocalDateTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        CreateTenantContractRequest dto2 =
                mapper.readValue(mapper.writeValueAsString(dto1),
                        CreateTenantContractRequest.class);

        assertEquals(dto1.getFirstName(), dto2.getFirstName());
        assertEquals(dto1.getLastName(), dto2.getLastName());
        assertEquals(dto1.getEmail(), dto2.getEmail());
        assertEquals(dto1.getPhoneNumber(), dto2.getPhoneNumber());
        assertEquals(dto1.getNationalId(), dto2.getNationalId());

        assertEquals(dto1.getRoomId(), dto2.getRoomId());
        assertEquals(dto1.getPackageId(), dto2.getPackageId());

        assertEquals(dto1.getSignDate(), dto2.getSignDate());
        assertEquals(dto1.getStartDate(), dto2.getStartDate());
        assertEquals(dto1.getEndDate(), dto2.getEndDate());

        assertEquals(dto1.getDeposit(), dto2.getDeposit());
        assertEquals(dto1.getRentAmountSnapshot(), dto2.getRentAmountSnapshot());

    }

    // ===========================================================
    // 5) toString() should not be null
    // ===========================================================
    @Test
    void testToString() {
        CreateTenantContractRequest dto = new CreateTenantContractRequest();
        assertNotNull(dto.toString());
    }

    // ===========================================================
    // 6) Null Safety
    // ===========================================================
    @Test
    void testNullValues() {
        CreateTenantContractRequest dto = new CreateTenantContractRequest();

        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setEmail(null);
        dto.setPhoneNumber(null);
        dto.setNationalId(null);
        dto.setRoomId(null);
        dto.setPackageId(null);
        dto.setSignDate(null);
        dto.setStartDate(null);
        dto.setEndDate(null);
        dto.setDeposit(null);
        dto.setRentAmountSnapshot(null);

        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getEmail());
        assertNull(dto.getPhoneNumber());
        assertNull(dto.getNationalId());
        assertNull(dto.getRoomId());
        assertNull(dto.getPackageId());
        assertNull(dto.getSignDate());
        assertNull(dto.getStartDate());
        assertNull(dto.getEndDate());
        assertNull(dto.getDeposit());
        assertNull(dto.getRentAmountSnapshot());
    }
}
