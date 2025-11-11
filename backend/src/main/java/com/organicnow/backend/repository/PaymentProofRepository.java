package com.organicnow.backend.repository;

import com.organicnow.backend.model.PaymentProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentProofRepository extends JpaRepository<PaymentProof, Long> {

    /**
     * หา Payment Proofs ทั้งหมดของ Payment Record
     */
    List<PaymentProof> findByPaymentRecordIdOrderByUploadedAtDesc(Long paymentRecordId);

    /**
     * หา Payment Proofs ตามประเภท
     */
    List<PaymentProof> findByProofTypeOrderByUploadedAtDesc(PaymentProof.ProofType proofType);

    /**
     * หา Payment Proof ตาม file path
     */
    Optional<PaymentProof> findByFilePath(String filePath);

    /**
     * หา Payment Proofs ของ Invoice (ผ่าน Payment Record)
     */
    @Query("SELECT pp FROM PaymentProof pp WHERE pp.paymentRecord.invoice.id = :invoiceId ORDER BY pp.uploadedAt DESC")
    List<PaymentProof> findByInvoiceId(@Param("invoiceId") Long invoiceId);

    /**
     * หา Payment Proofs ตามผู้อัปโหลด
     */
    List<PaymentProof> findByUploadedByContainingIgnoreCaseOrderByUploadedAtDesc(String uploadedBy);

    /**
     * นับจำนวน Payment Proofs ของ Payment Record
     */
    long countByPaymentRecordId(Long paymentRecordId);

    /**
     * ลบ Payment Proofs ทั้งหมดของ Payment Record
     */
    void deleteByPaymentRecordId(Long paymentRecordId);
}