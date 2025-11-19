package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UpdateTenantContractRequestTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void testGetterAndSetter() {
        UpdateTenantContractRequest dto = new UpdateTenantContractRequest();

        LocalDateTime now = LocalDateTime.now();

        dto.setFirstName("Alice");
        dto.setLastName("Smith");
        dto.setEmail("alice@example.com");
        dto.setPhoneNumber("0123456789");
        dto.setNationalId("1111111111111");

        dto.setRoomId(10L);
        dto.setPackageId(20L);

        dto.setSignDate(now);
        dto.setStartDate(now.plusDays(1));
        dto.setEndDate(now.plusMonths(12));

        dto.setStatus(1);
        dto.setDeposit(new BigDecimal("5000"));
        dto.setRentAmountSnapshot(new BigDecimal("8500"));

        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("0123456789", dto.getPhoneNumber());
        assertEquals("1111111111111", dto.getNationalId());

        assertEquals(10L, dto.getRoomId());
        assertEquals(20L, dto.getPackageId());

        assertEquals(now, dto.getSignDate());
        assertEquals(now.plusDays(1), dto.getStartDate());
        assertEquals(now.plusMonths(12), dto.getEndDate());

        assertEquals(1, dto.getStatus());
        assertEquals(new BigDecimal("5000"), dto.getDeposit());
        assertEquals(new BigDecimal("8500"), dto.getRentAmountSnapshot());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        UpdateTenantContractRequest dto = new UpdateTenantContractRequest(
                "Bob", "Lee", "bob@example.com", "0991112222", "2222222222222",
                5L, 8L,
                now, now.plusDays(3), now.plusMonths(6),
                2,
                new BigDecimal("3000"),
                new BigDecimal("9000")
        );

        assertEquals("Bob", dto.getFirstName());
        assertEquals("Lee", dto.getLastName());
        assertEquals("bob@example.com", dto.getEmail());
        assertEquals("0991112222", dto.getPhoneNumber());
        assertEquals("2222222222222", dto.getNationalId());

        assertEquals(5L, dto.getRoomId());
        assertEquals(8L, dto.getPackageId());

        assertEquals(now, dto.getSignDate());
        assertEquals(now.plusDays(3), dto.getStartDate());
        assertEquals(now.plusMonths(6), dto.getEndDate());

        assertEquals(2, dto.getStatus());
        assertEquals(new BigDecimal("3000"), dto.getDeposit());
        assertEquals(new BigDecimal("9000"), dto.getRentAmountSnapshot());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();

        UpdateTenantContractRequest dto = UpdateTenantContractRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .roomId(1L)
                .packageId(2L)
                .deposit(new BigDecimal("10000"))
                .build();

        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(1L, dto.getRoomId());
        assertEquals(2L, dto.getPackageId());
        assertEquals(new BigDecimal("10000"), dto.getDeposit());
        assertNull(dto.getNationalId());
        assertNull(dto.getStartDate());
        assertNull(dto.getRentAmountSnapshot());
    }

    @Test
    void testJsonSerialization() throws Exception {
        UpdateTenantContractRequest dto = UpdateTenantContractRequest.builder()
                .firstName("Test")
                .email("test@test.com")
                .roomId(99L)
                .deposit(new BigDecimal("4500"))
                .status(1)
                .build();

        String json = mapper.writeValueAsString(dto);

        assertTrue(json.contains("Test"));
        assertTrue(json.contains("test@test.com"));
        assertTrue(json.contains("99"));
        assertTrue(json.contains("4500"));
        assertTrue(json.contains("1"));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
                "firstName": "Mini",
                "lastName": "Cat",
                "email": "mini@cat.com",
                "roomId": 55,
                "packageId": 7,
                "deposit": 1200,
                "status": 0
            }
        """;

        UpdateTenantContractRequest dto = mapper.readValue(json, UpdateTenantContractRequest.class);

        assertEquals("Mini", dto.getFirstName());
        assertEquals("Cat", dto.getLastName());
        assertEquals("mini@cat.com", dto.getEmail());
        assertEquals(55L, dto.getRoomId());
        assertEquals(7L, dto.getPackageId());
        assertEquals(new BigDecimal("1200"), dto.getDeposit());
        assertEquals(0, dto.getStatus());
    }

    @Test
    void testNullFieldsSupported() {
        UpdateTenantContractRequest dto = new UpdateTenantContractRequest();

        assertNull(dto.getFirstName());
        assertNull(dto.getRoomId());
        assertNull(dto.getDeposit());
        assertNull(dto.getStartDate());
        assertNull(dto.getRentAmountSnapshot());
    }

    @Test
    void testToStringNotEmpty() {
        UpdateTenantContractRequest dto = new UpdateTenantContractRequest();
        assertNotNull(dto.toString());
        assertFalse(dto.toString().isBlank());
    }

    @Test
    void testEqualsAndHashCodeBasic() {
        UpdateTenantContractRequest a = new UpdateTenantContractRequest();
        UpdateTenantContractRequest b = new UpdateTenantContractRequest();

        // DTO ไม่มี equals() override → ต้องไม่เท่ากัน
        assertNotEquals(a, b);

        // hashCode() ก็ไม่จำเป็นต้องเท่ากัน
        assertNotEquals(a.hashCode(), b.hashCode());
    }

}
