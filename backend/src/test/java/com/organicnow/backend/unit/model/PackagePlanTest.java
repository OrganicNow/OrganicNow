package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.ContractType;
import com.organicnow.backend.model.PackagePlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PackagePlanTest {

    @Test
    void testGetterAndSetter() {
        ContractType type = new ContractType();
        type.setId(1L);
        type.setName("Monthly Contract");
        type.setDuration(12);

        PackagePlan plan = new PackagePlan();
        plan.setId(100L);
        plan.setContractType(type);
        plan.setPrice(BigDecimal.valueOf(5500.00));
        plan.setIsActive(1);
        plan.setRoomSize(35);

        assertEquals(100L, plan.getId());
        assertEquals(1L, plan.getContractType().getId());
        assertEquals("Monthly Contract", plan.getContractType().getName());
        assertEquals(BigDecimal.valueOf(5500.00), plan.getPrice());
        assertEquals(1, plan.getIsActive());
        assertEquals(35, plan.getRoomSize());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        ContractType type = new ContractType();
        type.setId(2L);
        type.setName("Yearly Contract");

        PackagePlan plan = PackagePlan.builder()
                .id(200L)
                .contractType(type)
                .price(BigDecimal.valueOf(12000.00))
                .isActive(1)
                .roomSize(45)
                .build();

        assertNotNull(plan);
        assertEquals(200L, plan.getId());
        assertEquals(2L, plan.getContractType().getId());
        assertEquals(BigDecimal.valueOf(12000.00), plan.getPrice());
        assertEquals(1, plan.getIsActive());
        assertEquals(45, plan.getRoomSize());
    }

    @Test
    void testAllArgsConstructor() {
        ContractType type = new ContractType();
        type.setId(3L);

        PackagePlan plan = new PackagePlan(
                300L,
                type,
                BigDecimal.valueOf(8000.50),
                0,
                28
        );

        assertEquals(300L, plan.getId());
        assertEquals(3L, plan.getContractType().getId());
        assertEquals(BigDecimal.valueOf(8000.50), plan.getPrice());
        assertEquals(0, plan.getIsActive());
        assertEquals(28, plan.getRoomSize());
    }

    @Test
    void testPriceShouldBeNonNegative() {
        PackagePlan plan = new PackagePlan();
        plan.setPrice(BigDecimal.ZERO);
        assertTrue(plan.getPrice().compareTo(BigDecimal.ZERO) >= 0);

        plan.setPrice(BigDecimal.valueOf(2500.00));
        assertTrue(plan.getPrice().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testToStringNotNull() {
        PackagePlan plan = new PackagePlan();
        plan.setRoomSize(50);
        plan.setIsActive(1);
        assertNotNull(plan.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        PackagePlan p1 = new PackagePlan();
        PackagePlan p2 = new PackagePlan();

        assertSame(p1, p1);   // อ้างอิงเดียวกัน
        assertNotSame(p1, p2); // คนละอ้างอิง
    }
}
