//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class UpdateInvoiceRequestTest {
//
//    @Test
//    void testUpdateInvoiceRequestConstructorAndGettersSetters() {
//        // Arrange
//        LocalDateTime dueDate = LocalDateTime.of(2025, 10, 20, 10, 0, 0, 0);
//        Integer invoiceStatus = 1; // 1 = ชำระแล้ว
//        LocalDateTime payDate = LocalDateTime.of(2025, 10, 15, 12, 0, 0, 0);
//        Integer payMethod = 2; // โอนเงิน
//        Integer subTotal = 2000;
//        Integer penaltyTotal = 100;
//        Integer netAmount = 2100;
//        LocalDateTime penaltyAppliedAt = LocalDateTime.of(2025, 10, 10, 9, 0, 0, 0);
//        String notes = "จ่ายก่อนกำหนด";
//
//        // Act: Create object using constructor
//        UpdateInvoiceRequest request = new UpdateInvoiceRequest(
//                dueDate, invoiceStatus, payDate, payMethod, subTotal, penaltyTotal, netAmount, penaltyAppliedAt, notes
//        );
//
//        // Assert: Verify that the values are correctly set
//        assertEquals(dueDate, request.getDueDate(), "Due date should match");
//        assertEquals(invoiceStatus, request.getInvoiceStatus(), "Invoice status should match");
//        assertEquals(payDate, request.getPayDate(), "Pay date should match");
//        assertEquals(payMethod, request.getPayMethod(), "Pay method should match");
//        assertEquals(subTotal, request.getSubTotal(), "Subtotal should match");
//        assertEquals(penaltyTotal, request.getPenaltyTotal(), "Penalty total should match");
//        assertEquals(netAmount, request.getNetAmount(), "Net amount should match");
//        assertEquals(penaltyAppliedAt, request.getPenaltyAppliedAt(), "Penalty applied at should match");
//        assertEquals(notes, request.getNotes(), "Notes should match");
//
//        // Testing setters
//        LocalDateTime newDueDate = LocalDateTime.of(2025, 10, 25, 10, 0, 0, 0);
//        request.setDueDate(newDueDate);
//        assertEquals(newDueDate, request.getDueDate(), "Due date should be updated");
//
//        String newNotes = "จ่ายช้ากว่าแผน";
//        request.setNotes(newNotes);
//        assertEquals(newNotes, request.getNotes(), "Notes should be updated");
//    }
//}
//
