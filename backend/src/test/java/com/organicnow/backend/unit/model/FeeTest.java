package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Fee;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeeTest {

    @Test
    void testGetterAndSetter() {
        Fee fee = new Fee();
        fee.setId(1L);
        fee.setFeeName("Water Fee");
        fee.setUnitFee(25);

        assertEquals(1L, fee.getId());
        assertEquals("Water Fee", fee.getFeeName());
        assertEquals(25, fee.getUnitFee());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Fee fee = Fee.builder()
                .id(2L)
                .feeName("Electricity Fee")
                .unitFee(100)
                .build();

        assertNotNull(fee);
        assertEquals(2L, fee.getId());
        assertEquals("Electricity Fee", fee.getFeeName());
        assertEquals(100, fee.getUnitFee());
    }

    @Test
    void testAllArgsConstructor() {
        Fee fee = new Fee(3L, "Internet Fee", 250);

        assertEquals(3L, fee.getId());
        assertEquals("Internet Fee", fee.getFeeName());
        assertEquals(250, fee.getUnitFee());
    }

    @Test
    void testDefaultConstructorAndSetter() {
        Fee fee = new Fee();
        assertNull(fee.getId());
        assertNull(fee.getFeeName());
        assertNull(fee.getUnitFee());

        fee.setFeeName("Cleaning Fee");
        fee.setUnitFee(50);

        assertEquals("Cleaning Fee", fee.getFeeName());
        assertEquals(50, fee.getUnitFee());
    }

    @Test
    void testToStringNotNull() {
        Fee fee = new Fee();
        fee.setFeeName("Parking Fee");
        assertNotNull(fee.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Fee f1 = new Fee();
        Fee f2 = new Fee();

        assertSame(f1, f1);   // อ้างอิงเดียวกัน
        assertNotSame(f1, f2); // คนละอ้างอิง
    }
}
