package com.organicnow.backend.unit.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.organicnow.backend.service.PdfStyleService;
import com.lowagie.text.pdf.BaseFont;


import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PdfStyleServiceTest {

    // ============================================================
    // createThaiBaseFonts()
    // ============================================================
    @Test
    void testCreateThaiBaseFonts_ShouldReturnFonts_WhenFilesExist() {

        BaseFont[] fonts = PdfStyleService.createThaiBaseFonts();

        assertNotNull(fonts);
        assertEquals(2, fonts.length);
        assertTrue(fonts[0] instanceof BaseFont);
    }

    // ============================================================
    // nvl()
    // ============================================================
    @Test
    void testNvl() {
        assertEquals("-", PdfStyleService.nvl(null));
        assertEquals("-", PdfStyleService.nvl("   "));
        assertEquals("ABC", PdfStyleService.nvl("ABC"));
    }

    // ============================================================
    // formatMoney()
    // ============================================================
    @Test
    void testFormatMoney() {
        assertEquals("0", PdfStyleService.formatMoney(null));
        assertEquals("1,000", PdfStyleService.formatMoney(1000));
        assertEquals("50", PdfStyleService.formatMoney(50));
    }

    // ============================================================
    // createInvoiceFonts() – fallback mode
    // ============================================================
    @Test
    void testCreateInvoiceFonts_Fallback_DefaultFonts() {
        Font[] fonts = PdfStyleService.createInvoiceFonts();

        assertNotNull(fonts);
        assertEquals(5, fonts.length);
        assertEquals(20, fonts[0].getSize()); // titleFont
        assertEquals(14, fonts[1].getSize()); // headerFont
    }

    // ============================================================
    // createHeaderCell()
    // ============================================================
    @Test
    void testCreateHeaderCell() {
        Font font = new Font();
        PdfPCell cell = PdfStyleService.createHeaderCell("Hello", font);

        Phrase phrase = cell.getPhrase();
        assertEquals("Hello", phrase.getContent());

        assertEquals(8f, cell.getPaddingLeft());
        assertEquals(8f, cell.getPaddingRight());
        assertEquals(8f, cell.getPaddingTop());
        assertEquals(8f, cell.getPaddingBottom());
    }

    // ============================================================
    // createDataCell()
    // ============================================================
    @Test
    void testCreateDataCell() {
        Font font = new Font();
        PdfPCell cell = PdfStyleService.createDataCell("X", font);

        Phrase phrase = cell.getPhrase();
        assertEquals("X", phrase.getContent());

        // PdfStyleService.setPadding(6f)
        assertEquals(6f, cell.getPaddingLeft());
    }

    // ============================================================
    // createLabelCell()
    // ============================================================
    @Test
    void testCreateLabelCell() {
        Font font = new Font();
        PdfPCell cell = PdfStyleService.createLabelCell("Label", font);

        assertEquals(PdfStyleService.LIGHT_GRAY, cell.getBackgroundColor());
        assertEquals(Element.ALIGN_LEFT, cell.getHorizontalAlignment());
        assertEquals(8f, cell.getPaddingLeft());
        assertEquals(8f, cell.getPaddingRight());
        assertEquals(8f, cell.getPaddingTop());
        assertEquals(8f, cell.getPaddingBottom());

    }

    // ============================================================
    // createValueCell()
    // ============================================================
    @Test
    void testCreateValueCell() {
        Font font = new Font();
        PdfPCell cell = PdfStyleService.createValueCell("Value", font);

        assertEquals(Element.ALIGN_LEFT, cell.getHorizontalAlignment());
        assertEquals(8f, cell.getPaddingLeft());
        assertEquals(8f, cell.getPaddingRight());
        assertEquals(8f, cell.getPaddingTop());
        assertEquals(8f, cell.getPaddingBottom());

    }

    // ============================================================
    // createStatusCell()
    // ============================================================
    @Test
    void testCreateStatusCell_Status0() {
        Font f = new Font();
        PdfPCell cell = PdfStyleService.createStatusCell("Pending", 0, f);
        assertEquals(new Color(255, 235, 235), cell.getBackgroundColor());
    }

    @Test
    void testCreateStatusCell_Status1() {
        Font f = new Font();
        PdfPCell cell = PdfStyleService.createStatusCell("Paid", 1, f);
        assertEquals(new Color(235, 255, 235), cell.getBackgroundColor());
    }

    @Test
    void testCreateStatusCell_Status2() {
        Font f = new Font();
        PdfPCell cell = PdfStyleService.createStatusCell("Cancelled", 2, f);
        assertEquals(new Color(245, 245, 245), cell.getBackgroundColor());
    }

    @Test
    void testCreateStatusCell_Default() {
        Font f = new Font();
        PdfPCell cell = PdfStyleService.createStatusCell("None", 99, f);
        assertEquals(Color.WHITE, cell.getBackgroundColor());
    }

    // ============================================================
    // addSeparatorLine()
    // ============================================================
    @Test
    void testAddSeparatorLine() throws Exception {
        Document mockDoc = mock(Document.class);

        PdfStyleService.addSeparatorLine(mockDoc);

        verify(mockDoc, atLeastOnce()).add(any());
    }

    // ============================================================
    // addCompanyHeader()
    // ============================================================
    @Test
    void testAddCompanyHeader() throws Exception {
        Document mockDoc = mock(Document.class);
        Font title = new Font();
        Font header = new Font();

        PdfStyleService.addCompanyHeader(mockDoc, title, header);

        ArgumentCaptor<Element> captor = ArgumentCaptor.forClass(Element.class);
        verify(mockDoc).add(captor.capture());

        Paragraph p = (Paragraph) captor.getValue();
        assertEquals("ORGANIC NOW", p.getContent());
        assertEquals(Element.ALIGN_CENTER, p.getAlignment());
    }
}
