package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.Invoice;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    @Test
    void testGetterAndSetter() {
        Contract contract = new Contract();
        contract.setId(1L);

        Invoice invoice = new Invoice();
        invoice.setId(100L);
        invoice.setContact(contract);
        invoice.setCreateDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        invoice.setDueDate(LocalDateTime.of(2025, 1, 10, 10, 0));
        invoice.setInvoiceStatus(1);
        invoice.setPayDate(LocalDateTime.of(2025, 1, 9, 12, 0));
        invoice.setPayMethod(2);
        invoice.setSubTotal(5000);
        invoice.setPenaltyTotal(200);
        invoice.setNetAmount(5200);
        invoice.setPreviousBalance(100);
        invoice.setPaidAmount(5200);
        invoice.setRemainingBalance(0);
        invoice.setPenaltyAppliedAt(LocalDateTime.of(2025, 1, 8, 9, 0));
        invoice.setPackageId(10L);
        invoice.setRequestedFloor(3);
        invoice.setRequestedRoom("A302");
        invoice.setRequestedRent(4500);
        invoice.setRequestedWater(100);
        invoice.setRequestedWaterUnit(5);
        invoice.setRequestedElectricity(200);
        invoice.setRequestedElectricityUnit(10);

        assertEquals(100L, invoice.getId());
        assertEquals(1L, invoice.getContact().getId());
        assertEquals(5000, invoice.getSubTotal());
        assertEquals(5200, invoice.getNetAmount());
        assertEquals(0, invoice.getRemainingBalance());
        assertEquals("A302", invoice.getRequestedRoom());
        assertEquals(3, invoice.getRequestedFloor());
        assertEquals(5, invoice.getRequestedWaterUnit());
        assertEquals(10, invoice.getRequestedElectricityUnit());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Contract contract = new Contract();
        contract.setId(2L);

        LocalDateTime createDate = LocalDateTime.of(2025, 5, 1, 10, 0);
        LocalDateTime dueDate = LocalDateTime.of(2025, 5, 15, 10, 0);

        Invoice invoice = Invoice.builder()
                .id(200L)
                .contact(contract)
                .createDate(createDate)
                .dueDate(dueDate)
                .invoiceStatus(1)
                .subTotal(7000)
                .penaltyTotal(100)
                .netAmount(7100)
                .requestedRoom("B201")
                .requestedRent(6500)
                .build();

        assertNotNull(invoice);
        assertEquals(200L, invoice.getId());
        assertEquals(2L, invoice.getContact().getId());
        assertEquals(7000, invoice.getSubTotal());
        assertEquals(7100, invoice.getNetAmount());
        assertEquals("B201", invoice.getRequestedRoom());
    }

    @Test
    void testAllArgsConstructor() {
        Contract contract = new Contract();
        contract.setId(3L);

        LocalDateTime create = LocalDateTime.of(2025, 2, 1, 10, 0);
        LocalDateTime due = LocalDateTime.of(2025, 2, 15, 10, 0);

        Invoice invoice = new Invoice(
                300L,
                contract,
                create,
                due,
                0,
                null,
                1,
                1000,
                0,
                1000,
                0,
                0,
                0,
                null,
                9L,
                2,
                "C205",
                8000,
                120,
                3,
                180,
                6
        );

        assertEquals(300L, invoice.getId());
        assertEquals(3L, invoice.getContact().getId());
        assertEquals(1000, invoice.getSubTotal());
        assertEquals(8000, invoice.getRequestedRent());
        assertEquals("C205", invoice.getRequestedRoom());
    }

    @Test
    void testPrePersistSetsDefaults() throws Exception {
        Invoice invoice = new Invoice();

        // ใช้ reflection เรียก protected method
        Method method = Invoice.class.getDeclaredMethod("onCreateDefaults");
        method.setAccessible(true);
        method.invoke(invoice);

        assertNotNull(invoice.getCreateDate());
        assertEquals(0, invoice.getSubTotal());
        assertEquals(0, invoice.getPenaltyTotal());
        assertEquals(0, invoice.getNetAmount());
        assertEquals(0, invoice.getPreviousBalance());
        assertEquals(0, invoice.getPaidAmount());
        assertEquals(0, invoice.getRemainingBalance());
        assertEquals(0, invoice.getInvoiceStatus());
    }

    @Test
    void testToStringNotNull() {
        Invoice invoice = new Invoice();
        invoice.setRequestedRoom("D101");
        assertNotNull(invoice.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Invoice i1 = new Invoice();
        Invoice i3 = new Invoice();

        assertSame(i1, i1);   // อ้างอิงเดียวกัน
        assertNotSame(i1, i3); // คนละอ้างอิง
    }
}
