package com.organicnow.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;

/**
 * Service สำหรับจัดการ Style และ Helper methods สำหรับ PDF
 * อ้างอิงจาก TenantContractService เพื่อให้มีลักษณะเดียวกัน
 */
@Service
public class PdfStyleService {

    // สีที่ใช้ในระบบ (ตรงกับ TenantContract)
    public static final Color PASTEL_BLUE = new Color(216, 239, 255);
    public static final Color BORDER_GRAY = new Color(200, 200, 200);
    public static final Color LIGHT_GRAY = new Color(240, 240, 240);
    public static final Color DARK_GRAY = new Color(60, 60, 60);
    public static final Color PRIMARY_TEXT = new Color(40, 40, 40);
    public static final Color SECONDARY_TEXT = new Color(80, 80, 80);
    public static final Color BODY_TEXT = new Color(30, 30, 30);

    /**
     * สร้าง BaseFont สำหรับภาษาไทย หรือ fallback เป็น default font
     * ใช้วิธีเดียวกับ TenantContract ที่ทำงานได้ใน Docker
     */
    public static BaseFont[] createThaiBaseFonts() {
        try {
            // ใช้วิธีเดียวกับ TenantContract - ใช้ THSarabunNew.ttf เป็นหลัก
            BaseFont regular = BaseFont.createFont("fonts/THSarabunNew.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bold = BaseFont.createFont("fonts/TH Sarabun New Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            
            System.out.println(">>> [PdfStyleService] Thai fonts (THSarabunNew) loaded successfully ✅");
            return new BaseFont[]{regular, bold};
        } catch (Exception e) {
            System.err.println(">>> [PdfStyleService] THSarabunNew fonts not found, trying Sarabun fallback: " + e.getMessage());
            // ลอง Sarabun เป็น fallback
            try {
                BaseFont regular = BaseFont.createFont("fonts/Sarabun-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                BaseFont bold = BaseFont.createFont("fonts/Sarabun-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                System.out.println(">>> [PdfStyleService] Sarabun fallback fonts loaded successfully ✅");
                return new BaseFont[]{regular, bold};
            } catch (Exception e2) {
                System.err.println(">>> [PdfStyleService] All font loading failed, using default fonts: " + e2.getMessage());
                return null;
            }
        }
    }

    /**
     * สร้างชุด Fonts สำหรับ Invoice (อ้างอิงจาก TenantContract)
     */
    public static Font[] createInvoiceFonts() {
        BaseFont[] baseFonts = createThaiBaseFonts();
        
        if (baseFonts != null) {
            // ใช้ฟอนต์ไทย
            return new Font[]{
                new Font(baseFonts[1], 20, Font.BOLD, PRIMARY_TEXT),    // titleFont
                new Font(baseFonts[1], 14, Font.BOLD, DARK_GRAY),       // headerFont
                new Font(baseFonts[1], 11, Font.BOLD, PRIMARY_TEXT),    // labelFont
                new Font(baseFonts[0], 11, Font.NORMAL, BODY_TEXT),     // normalFont
                new Font(baseFonts[0], 9, Font.NORMAL, SECONDARY_TEXT)  // smallFont
            };
        } else {
            // Fallback เป็น default fonts
            return new Font[]{
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_TEXT),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, DARK_GRAY),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY_TEXT),
                FontFactory.getFont(FontFactory.HELVETICA, 11, BODY_TEXT),
                FontFactory.getFont(FontFactory.HELVETICA, 9, SECONDARY_TEXT)
            };
        }
    }

    // Helper methods สำหรับสร้าง PDF Cell ต่าง ๆ (อ้างอิงจาก TenantContract)
    
    public static PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PASTEL_BLUE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1f);
        return cell;
    }

    public static PdfPCell createDataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1f);
        return cell;
    }

    public static PdfPCell createLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(8f);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1f);
        return cell;
    }

    public static PdfPCell createValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(8f);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(1f);
        return cell;
    }

    public static PdfPCell createSummaryLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    public static PdfPCell createSummaryValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    /**
     * สร้าง Company Header สำหรับ PDF (อ้างอิงจาก TenantContract)
     */
    public static void addCompanyHeader(Document document, Font titleFont, Font headerFont) throws DocumentException {
        Paragraph companyTitle = new Paragraph("ORGANIC NOW", titleFont);
        companyTitle.setAlignment(Element.ALIGN_CENTER);
        companyTitle.setSpacingAfter(5);
        document.add(companyTitle);

        
    }

    /**
     * สร้าง Separator Line
     */
    public static void addSeparatorLine(Document document) throws DocumentException {
        com.lowagie.text.pdf.draw.LineSeparator line = 
            new com.lowagie.text.pdf.draw.LineSeparator(0.7f, 100, BORDER_GRAY, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    /**
     * สร้าง Status Cell พร้อมสี background ตามสถานะ
     */
    public static PdfPCell createStatusCell(String statusText, int status, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(statusText, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(15);
        
        switch (status) {
            case 0: // ยังไม่ชำระ
                cell.setBackgroundColor(new Color(255, 235, 235)); // Light red
                break;
            case 1: // ชำระแล้ว
                cell.setBackgroundColor(new Color(235, 255, 235)); // Light green
                break;
            case 2: // ยกเลิก
                cell.setBackgroundColor(new Color(245, 245, 245)); // Light gray
                break;
            default:
                cell.setBackgroundColor(Color.WHITE);
                break;
        }
        
        return cell;
    }

    /**
     * Helper method สำหรับ null-safe string
     */
    public static String nvl(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    /**
     * Helper method สำหรับ format จำนวนเงิน
     */
    public static String formatMoney(Integer amount) {
        if (amount == null) return "0";
        return String.format("%,d", amount);
    }
}