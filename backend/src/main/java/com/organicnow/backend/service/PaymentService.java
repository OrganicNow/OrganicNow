package com.organicnow.backend.service;

import com.organicnow.backend.dto.*;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentProofRepository paymentProofRepository;
    private final InvoiceRepository invoiceRepository;
    
    // กำหนด directory สำหรับเก็บไฟล์หลักฐาน
    private final String UPLOAD_DIR = "uploads/payment-proofs/";

    public PaymentService(PaymentRecordRepository paymentRecordRepository,
                         PaymentProofRepository paymentProofRepository,
                         InvoiceRepository invoiceRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentProofRepository = paymentProofRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public PaymentRecordDto addPaymentRecord(CreatePaymentRecordRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + request.getInvoiceId()));

        PaymentRecord paymentRecord = PaymentRecord.builder()
                .invoice(invoice)
                .paymentAmount(request.getPaymentAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentRecord.PaymentStatus.PENDING) // เริ่มต้นเป็น PENDING
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
                .transactionReference(request.getTransactionReference())
                .notes(request.getNotes())
                .recordedBy(request.getRecordedBy())
                .build();

        PaymentRecord saved = paymentRecordRepository.save(paymentRecord);
        
        // ตรวจสอบและอัปเดตสถานะ Invoice
        updateInvoiceStatus(invoice);
        
        return PaymentRecordDto.fromEntity(saved);
    }

    public List<PaymentRecordDto> getPaymentRecordsByInvoice(Long invoiceId) {
        List<PaymentRecord> paymentRecords = paymentRecordRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId);
        return paymentRecords.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public PaymentRecordDto updatePaymentRecord(Long paymentRecordId, UpdatePaymentRecordRequest request) {
        PaymentRecord paymentRecord = paymentRecordRepository.findById(paymentRecordId)
                .orElseThrow(() -> new RuntimeException("Payment record not found: " + paymentRecordId));

        if (request.getPaymentAmount() != null) {
            paymentRecord.setPaymentAmount(request.getPaymentAmount());
        }
        if (request.getPaymentMethod() != null) {
            paymentRecord.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getPaymentStatus() != null) {
            paymentRecord.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getPaymentDate() != null) {
            paymentRecord.setPaymentDate(request.getPaymentDate());
        }
        if (request.getTransactionReference() != null) {
            paymentRecord.setTransactionReference(request.getTransactionReference());
        }
        if (request.getNotes() != null) {
            paymentRecord.setNotes(request.getNotes());
        }

        PaymentRecord saved = paymentRecordRepository.save(paymentRecord);
        
        // ตรวจสอบและอัปเดตสถานะ Invoice
        updateInvoiceStatus(saved.getInvoice());
        
        return mapToDto(saved);
    }

    @Transactional
    public void deletePaymentRecord(Long paymentRecordId) {
        PaymentRecord paymentRecord = paymentRecordRepository.findById(paymentRecordId)
                .orElseThrow(() -> new RuntimeException("Payment record not found: " + paymentRecordId));
        
        Invoice invoice = paymentRecord.getInvoice();
        
        // ลบ Payment Proofs ก่อน
        paymentProofRepository.deleteByPaymentRecordId(paymentRecordId);
        
        // ลบ Payment Record
        paymentRecordRepository.deleteById(paymentRecordId);
        
        // อัปเดตสถานะ Invoice
        updateInvoiceStatus(invoice);
    }

    @Transactional
    public PaymentProofDto uploadPaymentProof(Long paymentRecordId, MultipartFile file, 
                                              PaymentProof.ProofType proofType, String description, String uploadedBy) {
        PaymentRecord paymentRecord = paymentRecordRepository.findById(paymentRecordId)
                .orElseThrow(() -> new RuntimeException("Payment record not found: " + paymentRecordId));

        try {
            // สร้าง directory ถ้ายังไม่มี
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // สร้างชื่อไฟล์ใหม่เพื่อป้องกันชื่อซ้ำ
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = UPLOAD_DIR + newFileName;

            // บันทึกไฟล์
            Path targetPath = Paths.get(filePath);
            Files.copy(file.getInputStream(), targetPath);

            // บันทึกข้อมูลลงฐานข้อมูล
            PaymentProof paymentProof = PaymentProof.builder()
                    .paymentRecord(paymentRecord)
                    .fileName(originalFileName)
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .proofType(proofType)
                    .description(description)
                    .uploadedBy(uploadedBy)
                    .build();

            PaymentProof saved = paymentProofRepository.save(paymentProof);
            return PaymentProofDto.fromEntity(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<PaymentProofDto> getPaymentProofsByPaymentRecord(Long paymentRecordId) {
        List<PaymentProof> paymentProofs = paymentProofRepository.findByPaymentRecordIdOrderByUploadedAtDesc(paymentRecordId);
        return paymentProofs.stream()
                .map(PaymentProofDto::fromEntity)
                .toList();
    }

    @Transactional
    public void deletePaymentProof(Long paymentProofId) {
        PaymentProof paymentProof = paymentProofRepository.findById(paymentProofId)
                .orElseThrow(() -> new RuntimeException("Payment proof not found: " + paymentProofId));

        try {
            // ลบไฟล์จากระบบ
            Path filePath = Paths.get(paymentProof.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
        }

        // ลบจากฐานข้อมูล
        paymentProofRepository.deleteById(paymentProofId);
    }

    /**
     * ดาวน์โหลดหลักฐานการชำระเงิน
     */
    public org.springframework.core.io.Resource downloadPaymentProof(Long paymentProofId) {
        PaymentProof paymentProof = paymentProofRepository.findById(paymentProofId)
                .orElseThrow(() -> new RuntimeException("Payment proof not found: " + paymentProofId));

        try {
            Path filePath = Paths.get(paymentProof.getFilePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + paymentProof.getFileName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + paymentProof.getFileName());
        }
    }

    /**
     * ดึงข้อมูลหลักฐานการชำระเงินตาม ID
     */
    public PaymentProofDto getPaymentProofById(Long paymentProofId) {
        PaymentProof paymentProof = paymentProofRepository.findById(paymentProofId)
                .orElseThrow(() -> new RuntimeException("Payment proof not found: " + paymentProofId));
        
        return PaymentProofDto.fromEntity(paymentProof);
    }

    public BigDecimal getTotalPaidAmount(Long invoiceId) {
        return paymentRecordRepository.calculateTotalPaidAmount(invoiceId);
    }

    public BigDecimal getTotalPendingAmount(Long invoiceId) {
        return paymentRecordRepository.calculateTotalPendingAmount(invoiceId);
    }

    /**
     * อัปเดตสถานะ Invoice ตามการชำระเงิน
     */
    private void updateInvoiceStatus(Invoice invoice) {
        BigDecimal totalPaid = paymentRecordRepository.calculateTotalPaidAmount(invoice.getId());
        
        // ✅ คำนวณ NET amount จริงเหมือนกับ InvoiceServiceImpl.convertToDto
        int realSubTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : 0;
        int realPenalty = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
        BigDecimal realNetAmount = BigDecimal.valueOf(realSubTotal + realPenalty);

        System.out.println("💰 Updating Invoice #" + invoice.getId() + 
                         " Status - Paid: " + totalPaid + 
                         ", NET Required: " + realNetAmount + 
                         ", SubTotal: " + realSubTotal + 
                         ", Penalty: " + realPenalty);

        if (totalPaid.compareTo(realNetAmount) >= 0) {
            // ชำระครบแล้ว
            invoice.setInvoiceStatus(1); // Complete
            if (invoice.getPayDate() == null) {
                invoice.setPayDate(LocalDateTime.now());
            }
            System.out.println("✅ Invoice #" + invoice.getId() + " marked as COMPLETE");
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            // ชำระบางส่วนแล้ว แต่ยังไม่ครบ - ให้คงสถานะเป็น Incomplete
            invoice.setInvoiceStatus(0); // Incomplete
            System.out.println("⏳ Invoice #" + invoice.getId() + " remains INCOMPLETE (partial payment)");
        }

        invoiceRepository.save(invoice);
    }

    /**
     * แปลง PaymentRecord เป็น DTO พร้อม PaymentProofs
     */
    private PaymentRecordDto mapToDto(PaymentRecord paymentRecord) {
        PaymentRecordDto dto = PaymentRecordDto.fromEntity(paymentRecord);
        
        // เพิ่ม Payment Proofs
        List<PaymentProof> proofs = paymentProofRepository.findByPaymentRecordIdOrderByUploadedAtDesc(paymentRecord.getId());
        List<PaymentProofDto> proofDtos = proofs.stream()
                .map(PaymentProofDto::fromEntity)
                .toList();
        dto.setPaymentProofs(proofDtos);
        
        return dto;
    }
}