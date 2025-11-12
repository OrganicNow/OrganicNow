package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.ContractType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContractTypeTest {

    @Test
    void testGetterAndSetter() {
        ContractType type = new ContractType();
        type.setId(1L);
        type.setName("Short-Term Lease");
        type.setDuration(6);

        assertEquals(1L, type.getId());
        assertEquals("Short-Term Lease", type.getName());
        assertEquals(6, type.getDuration());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        ContractType type = ContractType.builder()
                .id(2L)
                .name("Long-Term Lease")
                .duration(12)
                .build();

        assertNotNull(type);
        assertEquals(2L, type.getId());
        assertEquals("Long-Term Lease", type.getName());
        assertEquals(12, type.getDuration());
    }

    @Test
    void testAllArgsConstructor() {
        ContractType type = new ContractType(3L, "Special Contract", 24);

        assertEquals(3L, type.getId());
        assertEquals("Special Contract", type.getName());
        assertEquals(24, type.getDuration());
    }

    @Test
    void testDefaultConstructorAndSetters() {
        ContractType type = new ContractType();
        assertNull(type.getId());
        assertNull(type.getName());
        assertNull(type.getDuration());

        type.setName("Trial Contract");
        type.setDuration(1);

        assertEquals("Trial Contract", type.getName());
        assertEquals(1, type.getDuration());
    }

    @Test
    void testToStringNotNull() {
        ContractType type = new ContractType();
        type.setName("Monthly");
        assertNotNull(type.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        ContractType t1 = new ContractType();
        ContractType t3 = new ContractType();

        assertSame(t1, t1);   // อ้างอิงเดียวกัน
        assertNotSame(t1, t3); // คนละอ้างอิง
    }
}
