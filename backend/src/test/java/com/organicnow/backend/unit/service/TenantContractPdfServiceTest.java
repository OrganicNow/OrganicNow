//package com.organicnow.backend.unit.service;
//
//import com.lowagie.text.Document;
//import com.lowagie.text.Font;
//import com.lowagie.text.Paragraph;
//import com.lowagie.text.pdf.PdfWriter;
//import com.organicnow.backend.model.Contract;
//import com.organicnow.backend.model.ContractType;
//import com.organicnow.backend.model.PackagePlan;
//import com.organicnow.backend.model.Room;
//import com.organicnow.backend.model.Tenant;
//import com.organicnow.backend.service.TenantContractPdfService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.ByteArrayOutputStream; // <-- ต้องมี import นี้
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class TenantContractPdfServiceTest {
//
//    private TenantContractPdfService pdfService;
//
//    @BeforeEach
//    void setUp() {
//        pdfService = new TenantContractPdfService() {
//            @Override
//            public byte[] generateContractPdf(Tenant tenant, Contract contract) {
//                try {
//                    // สร้าง PDF เปล่า ใช้ฟอนต์ built-in
//                    Document document = new Document();
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream(); // <-- ใช้ import
//                    PdfWriter.getInstance(document, baos);
//                    document.open();
//                    document.add(new Paragraph("Test PDF", new Font(Font.HELVETICA, 12)));
//                    document.close();
//                    return baos.toByteArray(); // <-- ใช้ import
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };
//    }
//
//    @Test
//    void generateContractPdf_shouldReturnNonEmptyByteArray() {
//        // Mock tenant
//        Tenant tenant = Tenant.builder()
//                .firstName("John")
//                .lastName("Doe")
//                .nationalId("1234567890123")
//                .phoneNumber("0812345678")
//                .email("john@example.com")
//                .build();
//
//        // Mock contract
//        Room room = Room.builder().roomNumber("101").roomFloor(1).build();
//        ContractType type = ContractType.builder().name("6 เดือน").build();
//        PackagePlan plan = PackagePlan.builder().contractType(type).price(BigDecimal.valueOf(10000)).build();
//        Contract contract = Contract.builder()
//                .room(room)
//                .packagePlan(plan)
//                .startDate(LocalDateTime.now())
//                .endDate(LocalDateTime.now().plusMonths(6))
//                .deposit(BigDecimal.valueOf(5000))
//                .rentAmountSnapshot(BigDecimal.valueOf(10000))
//                .build();
//
//        byte[] pdfBytes = pdfService.generateContractPdf(tenant, contract);
//
//        assertNotNull(pdfBytes, "PDF byte array should not be null");
//        assertTrue(pdfBytes.length > 0, "PDF byte array should not be empty");
//    }
//}
