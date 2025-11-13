//package com.organicnow.backend.unit.dto;
//
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class InvoiceDtoTest {
//
//    @Test
//    void testNoArgsConstructor() {
//        InvoiceDto invoiceDto = new InvoiceDto();
//
//        assertNull(invoiceDto.getId()); // ID is null by default
//        assertNull(invoiceDto.getContractDetails()); // Contract details are null by default
//        assertNull(invoiceDto.getCreateDate()); // Create date is null by default
//        assertNull(invoiceDto.getDueDate()); // Due date is null by default
//        assertEquals("ไม่ระบุ", invoiceDto.getPayMethodText()); // payMethod is null, should return "ไม่ระบุ"
//        assertEquals("Unknown", invoiceDto.getStatus()); // invoiceStatus is null, should return "Unknown"
//        assertNull(invoiceDto.getPayDate()); // Pay date is null by default
//        assertNull(invoiceDto.getSubTotal()); // Sub_total is null by default
//        assertNull(invoiceDto.getPenaltyTotal()); // Penalty total is null by default
//        assertNull(invoiceDto.getNetAmount()); // Net amount is null by default
//        assertNull(invoiceDto.getPenaltyAppliedAt()); // Penalty applied at is null by default
//    }
//
//
//
//
//
//    @Test
//    void testBuilder() {
//        // Arrange
//        Long expectedId = 1L;
//        Long expectedContractId = 100L;
//        String expectedContractDetails = "Sample Contract";
//        LocalDateTime expectedCreateDate = LocalDateTime.now();
//        LocalDateTime expectedDueDate = LocalDateTime.of(2025, 5, 31, 12, 0, 0, 0);
//        Integer expectedInvoiceStatus = 1;  // Complete
//        String expectedStatusText = "Complete";
//        Integer expectedSubTotal = 5000;
//        Integer expectedPenaltyTotal = 100;
//        Integer expectedNetAmount = 5100;
//
//        // Act
//        InvoiceDto invoiceDto = InvoiceDto.builder()
//                .id(expectedId)
//                .contractId(expectedContractId)
//                .contractDetails(expectedContractDetails)
//                .createDate(expectedCreateDate)
//                .dueDate(expectedDueDate)
//                .invoiceStatus(expectedInvoiceStatus)
//                .statusText(expectedStatusText)
//                .subTotal(expectedSubTotal)
//                .penaltyTotal(expectedPenaltyTotal)
//                .netAmount(expectedNetAmount)
//                .build();
//
//        // Assert
//        assertEquals(expectedId, invoiceDto.getId());
//        assertEquals(expectedContractId, invoiceDto.getContractId());
//        assertEquals(expectedContractDetails, invoiceDto.getContractDetails());
//        assertEquals(expectedCreateDate, invoiceDto.getCreateDate());
//        assertEquals(expectedDueDate, invoiceDto.getDueDate());
//        assertEquals(expectedInvoiceStatus, invoiceDto.getInvoiceStatus());
//        assertEquals(expectedStatusText, invoiceDto.getStatusText());
//        assertEquals(expectedSubTotal, invoiceDto.getSubTotal());
//        assertEquals(expectedPenaltyTotal, invoiceDto.getPenaltyTotal());
//        assertEquals(expectedNetAmount, invoiceDto.getNetAmount());
//    }
//
//    @Test
//    void testStatusText() {
//        // Arrange
//        InvoiceDto invoiceDto = InvoiceDto.builder().invoiceStatus(0).build(); // Incomplete
//
//        // Act
//        String statusText = invoiceDto.getStatusText();
//
//        // Assert
//        assertEquals("Incomplete", statusText);
//    }
//
//    @Test
//    void testPayMethodText() {
//        // Arrange
//        InvoiceDto invoiceDto = InvoiceDto.builder().payMethod(2).build();  // Bank Transfer
//
//        // Act
//        String payMethodText = invoiceDto.getPayMethodText();
//
//        // Assert
//        assertEquals("โอนเงิน", payMethodText);
//    }
//
//    @Test
//    void testPenalty() {
//        // Arrange
//        InvoiceDto invoiceDto = InvoiceDto.builder().penaltyTotal(100).build();  // Penalty is greater than 0
//
//        // Act
//        Integer penalty = invoiceDto.getPenalty();
//
//        // Assert
//        assertEquals(1, penalty);
//    }
//
//    @Test
//    void testAmount() {
//        // Arrange
//        InvoiceDto invoiceDto = InvoiceDto.builder().netAmount(5100).build();
//
//        // Act
//        Integer amount = invoiceDto.getAmount();
//
//        // Assert
//        assertEquals(5100, amount);
//    }
//
//    @Test
//    void testSetStatusFromString() {
//        // Arrange
//        InvoiceDto invoiceDto = new InvoiceDto();
//        invoiceDto.setStatus("Complete");
//
//        // Act
//        Integer status = invoiceDto.getInvoiceStatus();
//
//        // Assert
//        assertEquals(1, status);  // Status should be set to 1 (Complete)
//    }
//}
