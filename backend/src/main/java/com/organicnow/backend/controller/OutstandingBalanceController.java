package com.organicnow.backend.controller;

import com.organicnow.backend.service.OutstandingBalanceService;
import com.organicnow.backend.service.OutstandingBalanceService.OutstandingBalanceSummary;
import com.organicnow.backend.model.Invoice;
import com.organicnow.backend.model.PaymentRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller สำหรับจัดการยอดค้างชำระ (Outstanding Balance)
 */
@RestController
@RequestMapping("/outstanding-balance")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://app.localtest.me"}, allowCredentials = "true")
public class OutstandingBalanceController {

    private final OutstandingBalanceService outstandingBalanceService;

    public OutstandingBalanceController(OutstandingBalanceService outstandingBalanceService) {
        this.outstandingBalanceService = outstandingBalanceService;
    }

    /**
     * ดึงสรุปยอดค้างชำระของ Contract
     */
    @GetMapping("/contract/{contractId}/summary")
    public ResponseEntity<OutstandingBalanceSummary> getOutstandingBalanceSummary(@PathVariable Long contractId) {
        try {
            OutstandingBalanceSummary summary = outstandingBalanceService.getOutstandingBalanceSummary(contractId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ดึงรายการใบแจ้งหนี้ที่มียอดค้างชำระ
     */
    @GetMapping("/contract/{contractId}/invoices")
    public ResponseEntity<List<Invoice>> getOutstandingInvoices(@PathVariable Long contractId) {
        try {
            List<Invoice> invoices = outstandingBalanceService.getOutstandingInvoices(contractId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * คำนวณยอดค้างชำระปัจจุบัน
     */
    @GetMapping("/contract/{contractId}/calculate")
    public ResponseEntity<Map<String, Object>> calculateOutstandingBalance(@PathVariable Long contractId) {
        try {
            Integer outstandingBalance = outstandingBalanceService.calculateOutstandingBalance(contractId);
            return ResponseEntity.ok(Map.of("outstandingBalance", outstandingBalance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * คำนวณยอดค้างชำระปัจจุบัน (Short URL สำหรับ Frontend)
     */
    @GetMapping("/calculate/{contractId}")
    public ResponseEntity<Integer> calculateOutstandingBalanceShort(@PathVariable Long contractId) {
        System.out.println("🔍 API /outstanding-balance/calculate/" + contractId + " called");
        try {
            Integer outstandingBalance = outstandingBalanceService.calculateOutstandingBalance(contractId);
            System.out.println("✅ Outstanding Balance Result: " + outstandingBalance + " บาท");
            return ResponseEntity.ok(outstandingBalance);
        } catch (Exception e) {
            System.err.println("❌ Error in calculateOutstandingBalanceShort: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * บันทึกการชำระเงิน
     */
    @PostMapping("/invoice/{invoiceId}/payment")
    public ResponseEntity<PaymentRecord> recordPayment(
            @PathVariable Long invoiceId,
            @RequestBody PaymentRequest request) {
        try {
            PaymentRecord payment = outstandingBalanceService.recordPayment(
                invoiceId,
                request.getPaymentAmount(),
                request.getPaymentMethod(),
                request.getNotes()
            );
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DTO สำหรับ Payment Request
     */
    public static class PaymentRequest {
        private BigDecimal paymentAmount;
        private PaymentRecord.PaymentMethod paymentMethod;
        private String notes;

        // Getters and Setters
        public BigDecimal getPaymentAmount() {
            return paymentAmount;
        }

        public void setPaymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
        }

        public PaymentRecord.PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(PaymentRecord.PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}