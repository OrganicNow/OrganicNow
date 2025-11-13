package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Fee;
import com.organicnow.backend.model.Invoice;
import com.organicnow.backend.model.InvoiceItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceItemTest {

    @Test
    void testGetterAndSetter() {
        Fee fee = new Fee();
        fee.setId(1L);
        fee.setFeeName("Water Fee");

        Invoice invoice = new Invoice();
        invoice.setId(2L);

        InvoiceItem item = new InvoiceItem();
        item.setId(100L);
        item.setFee(fee);
        item.setInvoice(invoice);
        item.setTotalFee(300);

        assertEquals(100L, item.getId());
        assertEquals(1L, item.getFee().getId());
        assertEquals("Water Fee", item.getFee().getFeeName());
        assertEquals(2L, item.getInvoice().getId());
        assertEquals(300, item.getTotalFee());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Fee fee = new Fee();
        fee.setId(5L);
        fee.setFeeName("Electricity Fee");

        Invoice invoice = new Invoice();
        invoice.setId(6L);

        InvoiceItem item = InvoiceItem.builder()
                .id(101L)
                .fee(fee)
                .invoice(invoice)
                .totalFee(500)
                .build();

        assertNotNull(item);
        assertEquals(101L, item.getId());
        assertEquals(5L, item.getFee().getId());
        assertEquals(6L, item.getInvoice().getId());
        assertEquals(500, item.getTotalFee());
    }

    @Test
    void testAllArgsConstructor() {
        Fee fee = new Fee();
        fee.setId(11L);
        Invoice invoice = new Invoice();
        invoice.setId(12L);

        InvoiceItem item = new InvoiceItem(13L, fee, invoice, 700);

        assertEquals(13L, item.getId());
        assertEquals(11L, item.getFee().getId());
        assertEquals(12L, item.getInvoice().getId());
        assertEquals(700, item.getTotalFee());
    }

    @Test
    void testTotalFeePositiveOrZero() {
        InvoiceItem item = new InvoiceItem();
        item.setTotalFee(0);
        assertEquals(0, item.getTotalFee());

        item.setTotalFee(250);
        assertTrue(item.getTotalFee() >= 0);
    }

    @Test
    void testToStringNotNull() {
        InvoiceItem item = new InvoiceItem();
        item.setTotalFee(999);
        assertNotNull(item.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        InvoiceItem i1 = new InvoiceItem();
        InvoiceItem i3 = new InvoiceItem();

        assertSame(i1, i1);   // อ้างอิงเดียวกัน
        assertNotSame(i1, i3); // คนละอ้างอิง
    }
}
