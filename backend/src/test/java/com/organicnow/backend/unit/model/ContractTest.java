package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.model.PackagePlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContractTest {

    @Test
    void testGetterAndSetter() {
        Contract contract = new Contract();
        Room room = new Room();
        Tenant tenant = new Tenant();
        PackagePlan packagePlan = new PackagePlan();

        room.setId(1L);
        tenant.setId(2L);
        packagePlan.setId(3L);

        contract.setId(10L);
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setPackagePlan(packagePlan);
        contract.setSignDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        contract.setStartDate(LocalDateTime.of(2025, 2, 1, 10, 0));
        contract.setEndDate(LocalDateTime.of(2026, 2, 1, 10, 0));
        contract.setStatus(1);
        contract.setDeposit(new BigDecimal("5000.00"));
        contract.setRentAmountSnapshot(new BigDecimal("8500.00"));

        assertEquals(10L, contract.getId());
        assertEquals(1L, contract.getRoom().getId());
        assertEquals(2L, contract.getTenant().getId());
        assertEquals(3L, contract.getPackagePlan().getId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), contract.getSignDate());
        assertEquals(LocalDateTime.of(2025, 2, 1, 10, 0), contract.getStartDate());
        assertEquals(LocalDateTime.of(2026, 2, 1, 10, 0), contract.getEndDate());
        assertEquals(1, contract.getStatus());
        assertEquals(new BigDecimal("5000.00"), contract.getDeposit());
        assertEquals(new BigDecimal("8500.00"), contract.getRentAmountSnapshot());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Room room = new Room();
        room.setId(11L);

        Tenant tenant = new Tenant();
        tenant.setId(22L);

        PackagePlan pkg = new PackagePlan();
        pkg.setId(33L);

        LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 1, 10, 0);

        Contract contract = Contract.builder()
                .id(44L)
                .room(room)
                .tenant(tenant)
                .packagePlan(pkg)
                .signDate(LocalDateTime.of(2025, 4, 1, 10, 0))
                .startDate(start)
                .endDate(end)
                .status(1)
                .deposit(new BigDecimal("10000.00"))
                .rentAmountSnapshot(new BigDecimal("12000.00"))
                .build();

        assertNotNull(contract);
        assertEquals(44L, contract.getId());
        assertEquals(11L, contract.getRoom().getId());
        assertEquals(22L, contract.getTenant().getId());
        assertEquals(33L, contract.getPackagePlan().getId());
        assertEquals(new BigDecimal("10000.00"), contract.getDeposit());
        assertEquals(new BigDecimal("12000.00"), contract.getRentAmountSnapshot());
        assertEquals(1, contract.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        Room room = new Room();
        Tenant tenant = new Tenant();
        PackagePlan pkg = new PackagePlan();
        LocalDateTime start = LocalDateTime.of(2025, 3, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 1, 10, 0);

        Contract contract = new Contract(
                99L,
                room,
                tenant,
                pkg,
                LocalDateTime.of(2025, 2, 15, 10, 0),
                start,
                end,
                2,
                new BigDecimal("7500.00"),
                new BigDecimal("9500.00")
        );

        assertEquals(99L, contract.getId());
        assertEquals(room, contract.getRoom());
        assertEquals(tenant, contract.getTenant());
        assertEquals(pkg, contract.getPackagePlan());
        assertEquals(2, contract.getStatus());
        assertEquals(new BigDecimal("7500.00"), contract.getDeposit());
        assertEquals(new BigDecimal("9500.00"), contract.getRentAmountSnapshot());
    }

    @Test
    void testToStringNotNull() {
        Contract contract = new Contract();
        contract.setStatus(1);
        assertNotNull(contract.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Contract c1 = new Contract();
        Contract c3 = new Contract();

        assertSame(c1, c1);   // อ้างอิงเดียวกัน
        assertNotSame(c1, c3); // คนละอ้างอิง
    }
}
