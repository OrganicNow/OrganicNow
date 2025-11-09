package com.organicnow.backend.controller;

import com.organicnow.backend.dto.*;
import com.organicnow.backend.model.PaymentProof;
import com.organicnow.backend.model.PaymentRecord;
import com.organicnow.backend.service.PaymentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * เพิ่มการบันทึกการชำระเงิน
     */
    @PostMapping("/records")
    public ResponseEntity<PaymentRecordDto> addPaymentRecord(@RequestBody CreatePaymentRecordRequest request) {
        try {
            PaymentRecordDto result = paymentService.addPaymentRecord(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * ดูรายการการชำระเงินของ Invoice
     */
    @GetMapping("/records/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentRecordDto>> getPaymentRecordsByInvoice(@PathVariable Long invoiceId) {
        try {
            List<PaymentRecordDto> paymentRecords = paymentService.getPaymentRecordsByInvoice(invoiceId);
            return ResponseEntity.ok(paymentRecords);
        } catch (Exception e) {
            // Return empty list if service fails
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ดูประเภทวิธีการชำระเงินที่มี
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<Map<String, String>> getPaymentMethods() {
        try {
            Map<String, String> paymentMethods = Arrays.stream(PaymentRecord.PaymentMethod.values())
                    .collect(Collectors.toMap(
                        Enum::name,
                        PaymentRecord.PaymentMethod::getDisplayName
                    ));
            return ResponseEntity.ok(paymentMethods);
        } catch (Exception e) {
            // Return basic payment methods if enum fails
            Map<String, String> fallback = Map.of(
                "CASH", "เงินสด",
                "BANK_TRANSFER", "โอนเงิน",
                "PROMPTPAY", "พร้อมเพย์"
            );
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * ดูประเภทสถานะการชำระเงินที่มี
     */
    @GetMapping("/payment-statuses")
    public ResponseEntity<Map<String, String>> getPaymentStatuses() {
        try {
            Map<String, String> paymentStatuses = Arrays.stream(PaymentRecord.PaymentStatus.values())
                    .collect(Collectors.toMap(
                        Enum::name,
                        PaymentRecord.PaymentStatus::getDisplayName
                    ));
            return ResponseEntity.ok(paymentStatuses);
        } catch (Exception e) {
            // Return basic status if enum fails  
            Map<String, String> fallback = Map.of(
                "PENDING", "รอยืนยัน",
                "CONFIRMED", "ยืนยันแล้ว", 
                "REJECTED", "ปฏิเสธ"
            );
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * แก้ไขการบันทึกการชำระเงิน
     */
    @PutMapping("/records/{paymentRecordId}")
    public ResponseEntity<PaymentRecordDto> updatePaymentRecord(
            @PathVariable Long paymentRecordId,
            @RequestBody UpdatePaymentRecordRequest request) {
        
        PaymentRecordDto result = paymentService.updatePaymentRecord(paymentRecordId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * อัปเดตสถานะการชำระเงิน
     */
    @PutMapping("/records/{paymentRecordId}/status")
    public ResponseEntity<PaymentRecordDto> updatePaymentStatus(
            @PathVariable Long paymentRecordId,
            @RequestParam("status") PaymentRecord.PaymentStatus status) {
        
        UpdatePaymentRecordRequest request = UpdatePaymentRecordRequest.builder()
                .paymentStatus(status)
                .build();
        
        PaymentRecordDto result = paymentService.updatePaymentRecord(paymentRecordId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * ลบการบันทึกการชำระเงิน
     */
    @DeleteMapping("/records/{paymentRecordId}")
    public ResponseEntity<Void> deletePaymentRecord(@PathVariable Long paymentRecordId) {
        paymentService.deletePaymentRecord(paymentRecordId);
        return ResponseEntity.ok().build();
    }

    /**
     * อัปโหลดหลักฐานการชำระเงิน
     */
    @PostMapping("/records/{paymentRecordId}/proofs")
    public ResponseEntity<PaymentProofDto> uploadPaymentProof(
            @PathVariable Long paymentRecordId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("proofType") PaymentProof.ProofType proofType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(null);
            }
            
            PaymentProofDto result = paymentService.uploadPaymentProof(
                paymentRecordId, file, proofType, description, uploadedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * ดูหลักฐานการชำระเงินทั้งหมดของ Payment Record
     */
    @GetMapping("/records/{paymentRecordId}/proofs")
    public ResponseEntity<List<PaymentProofDto>> getPaymentProofsByPaymentRecord(@PathVariable Long paymentRecordId) {
        List<PaymentProofDto> paymentProofs = paymentService.getPaymentProofsByPaymentRecord(paymentRecordId);
        return ResponseEntity.ok(paymentProofs);
    }

    /**
     * ดาวน์โหลด/ดูหลักฐานการชำระเงิน
     */
    @GetMapping("/proofs/{paymentProofId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadPaymentProof(@PathVariable Long paymentProofId) {
        try {
            org.springframework.core.io.Resource file = paymentService.downloadPaymentProof(paymentProofId);
            
            // Get proof details for filename
            PaymentProofDto proof = paymentService.getPaymentProofById(paymentProofId);
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + proof.getOriginalFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ลบหลักฐานการชำระเงิน
     */
    @DeleteMapping("/proofs/{paymentProofId}")
    public ResponseEntity<Void> deletePaymentProof(@PathVariable Long paymentProofId) {
        paymentService.deletePaymentProof(paymentProofId);
        return ResponseEntity.ok().build();
    }

    /**
     * ดูสรุปการชำระเงินของ Invoice
     */
    @GetMapping("/summary/invoice/{invoiceId}")
    public ResponseEntity<Map<String, Object>> getPaymentSummary(@PathVariable Long invoiceId) {
        BigDecimal totalPaid = paymentService.getTotalPaidAmount(invoiceId);
        BigDecimal totalPending = paymentService.getTotalPendingAmount(invoiceId);
        
        Map<String, Object> summary = Map.of(
            "totalPaid", totalPaid,
            "totalPending", totalPending,
            "totalReceived", totalPaid.add(totalPending)
        );
        
        return ResponseEntity.ok(summary);
    }

    /**
     * ดูประเภทหลักฐานการชำระเงินที่มี
     */
    @GetMapping("/proof-types")
    public ResponseEntity<Map<String, String>> getProofTypes() {
        try {
            Map<String, String> proofTypes = Arrays.stream(PaymentProof.ProofType.values())
                    .collect(Collectors.toMap(
                        Enum::name,
                        PaymentProof.ProofType::getDisplayName
                    ));
            return ResponseEntity.ok(proofTypes);
        } catch (Exception e) {
            // Return basic proof types if enum fails
            Map<String, String> fallback = Map.of(
                "BANK_SLIP", "สลิปโอนเงิน",
                "RECEIPT", "ใบเสร็จ",
                "OTHER", "อื่นๆ"
            );
            return ResponseEntity.ok(fallback);
        }
    }
}