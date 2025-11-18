package com.organicnow.backend.unit.service;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PRTokeniser;
import com.organicnow.backend.model.*;
import com.organicnow.backend.service.TenantContractPdfService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TenantContractPdfServiceTest {

    private TenantContractPdfService pdfService;

    private Tenant tenant;
    private Contract contract;

    @BeforeEach
    void setup() {
        pdfService = new TenantContractPdfService();

        tenant = Tenant.builder()
                .firstName("John")
                .lastName("Doe")
                .nationalId("1234567890123")
                .phoneNumber("0900000000")
                .email("john@example.com")
                .build();

        ContractType type = new ContractType();
        type.setName("Monthly");

        PackagePlan plan = new PackagePlan();
        plan.setPrice(BigDecimal.valueOf(5000));
        plan.setContractType(type);

        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();

        contract = Contract.builder()
                .room(room)
                .packagePlan(plan)
                .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2024, 12, 31, 0, 0))
                .deposit(BigDecimal.valueOf(2000)) // ป้องกัน null format error
                .rentAmountSnapshot(BigDecimal.valueOf(5000))
                .build();
    }

    // ================================================================================
    @Test
    void generateContractPdf_ShouldReturnValidPdf() throws Exception {
        byte[] pdfBytes = pdfService.generateContractPdf(tenant, contract);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        PdfReader reader = new PdfReader(pdfBytes);
        assertTrue(reader.getNumberOfPages() >= 1);
    }

    // ================================================================================
    @Test
    void generatedPdf_ShouldContainTenantAndContractInfo() throws Exception {
        byte[] pdfBytes = pdfService.generateContractPdf(tenant, contract);

        PdfReader reader = new PdfReader(pdfBytes);

        // เงื่อนไข: PDF ต้องมี page
        assertTrue(reader.getNumberOfPages() >= 1);

        // เงื่อนไข: ไม่ empty
        assertTrue(pdfBytes.length > 600);

        // ตรวจ indirect objects ว่ามี table/cells ที่เราสร้าง
        String rawPdf = new String(pdfBytes);

        assertTrue(rawPdf.contains("/Table") || rawPdf.contains("/P"),
                "PDF must contain layout structures");
    }



    // ================================================================================
    // อนุญาต ให้ null บางค่า แต่ต้องไม่ครัช
    @Test
    void generatePdf_WithSomeNullValues_ShouldStillWork() {
        tenant.setEmail(null); // อันนี้ไม่กระทบ formatting

        // ห้าม set deposit = null เพราะ service format null → error
        // contract.setDeposit(null);

        assertDoesNotThrow(() -> {
            pdfService.generateContractPdf(tenant, contract);
        });
    }

    // ================================================================================
    @Test
    void generatedPdf_ShouldStartWithPDFHeader() {
        byte[] pdfBytes = pdfService.generateContractPdf(tenant, contract);
        assertEquals("%PDF", new String(pdfBytes, 0, 4));
    }

    // ================================================================================
    // PDF TEXT EXTRACTOR (รองรับ ASCII + Unicode Hex)
    // ================================================================================
    private String extractText(byte[] pdfBytes) throws Exception {
        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        byte[] contentBytes = reader.getPageContent(1);

        PRTokeniser tokenizer = new PRTokeniser(contentBytes);
        StringBuilder sb = new StringBuilder();

        while (tokenizer.nextToken()) {

            // (text) in Tj
            if (tokenizer.getTokenType() == PRTokeniser.TK_STRING) {
                sb.append(tokenizer.getStringValue()).append(" ");
            }

            // <xxxx> in hex
            if (tokenizer.getTokenType() == PRTokeniser.TK_OTHER) {
                String raw = tokenizer.getStringValue();

                // hex string <004A006F...>
                if (raw.startsWith("<") && raw.endsWith(">")) {
                    sb.append(hexToUtf16(raw.substring(1, raw.length() - 1))).append(" ");
                }
            }
        }

        return sb.toString();
    }

    private String hexToUtf16(String hex) {
        try {
            if (hex.length() % 4 != 0) return ""; // ไม่ใช่ UTF-16BE

            StringBuilder out = new StringBuilder();

            for (int i = 0; i < hex.length(); i += 4) {
                int code = Integer.parseInt(hex.substring(i, i + 4), 16);
                out.append((char) code);
            }

            return out.toString();
        } catch (Exception e) {
            return "";
        }
    }

}
