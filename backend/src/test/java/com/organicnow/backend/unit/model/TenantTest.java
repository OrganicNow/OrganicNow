package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Tenant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    void testGetterAndSetter() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setFirstName("John");
        tenant.setLastName("Doe");
        tenant.setPhoneNumber("0812345678");
        tenant.setEmail("john.doe@example.com");
        tenant.setNationalId("1234567890123");

        assertEquals(1L, tenant.getId());
        assertEquals("John", tenant.getFirstName());
        assertEquals("Doe", tenant.getLastName());
        assertEquals("0812345678", tenant.getPhoneNumber());
        assertEquals("john.doe@example.com", tenant.getEmail());
        assertEquals("1234567890123", tenant.getNationalId());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Tenant tenant = Tenant.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0899999999")
                .email("jane.smith@example.com")
                .nationalId("9876543210987")
                .build();

        assertNotNull(tenant);
        assertEquals(2L, tenant.getId());
        assertEquals("Jane", tenant.getFirstName());
        assertEquals("Smith", tenant.getLastName());
        assertEquals("0899999999", tenant.getPhoneNumber());
        assertEquals("jane.smith@example.com", tenant.getEmail());
        assertEquals("9876543210987", tenant.getNationalId());
    }

    @Test
    void testAllArgsConstructor() {
        Tenant tenant = new Tenant(
                3L,
                "Alice",
                "Wong",
                "0823456789",
                "alice@example.com",
                "5556667778889"
        );

        assertEquals(3L, tenant.getId());
        assertEquals("Alice", tenant.getFirstName());
        assertEquals("Wong", tenant.getLastName());
        assertEquals("0823456789", tenant.getPhoneNumber());
        assertEquals("alice@example.com", tenant.getEmail());
        assertEquals("5556667778889", tenant.getNationalId());
    }

    @Test
    void testEmailFormat() {
        Tenant tenant = new Tenant();
        tenant.setEmail("valid@email.com");
        assertTrue(tenant.getEmail().contains("@"));

        tenant.setEmail("invalidemail.com");
        assertFalse(tenant.getEmail().startsWith("@"));
    }

    @Test
    void testToStringNotNull() {
        Tenant tenant = new Tenant();
        tenant.setFirstName("Mark");
        assertNotNull(tenant.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Tenant t1 = new Tenant();
        Tenant t2 = new Tenant();

        assertSame(t1, t1);
        assertNotSame(t1, t2);
    }
}
