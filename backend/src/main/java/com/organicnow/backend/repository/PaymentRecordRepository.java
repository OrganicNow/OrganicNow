package com.organicnow.backend.repository;

import com.organicnow.backend.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    /**
     * หา Payment Records ทั้งหมดของ Invoice
     */
    List<PaymentRecord> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    /**
     * หา Payment Records ตามสถานะ
     */
    List<PaymentRecord> findByPaymentStatusOrderByPaymentDateDesc(PaymentRecord.PaymentStatus paymentStatus);

    /**
     * หา Payment Records ตามวิธีการชำระ
     */
    List<PaymentRecord> findByPaymentMethodOrderByPaymentDateDesc(PaymentRecord.PaymentMethod paymentMethod);

    /**
     * หา Payment Records ในช่วงวันที่
     */
    List<PaymentRecord> findByPaymentDateBetweenOrderByPaymentDateDesc(
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    /**
     * คำนวณยอดรวมที่ชำระแล้วของ Invoice (เฉพาะสถานะ CONFIRMED)
     */
    @Query("SELECT COALESCE(SUM(pr.paymentAmount), 0) FROM PaymentRecord pr WHERE pr.invoice.id = :invoiceId AND pr.paymentStatus = 'CONFIRMED'")
    BigDecimal calculateTotalPaidAmount(@Param("invoiceId") Long invoiceId);

    /**
     * คำนวณยอดรวมที่รอการยืนยัน
     */
    @Query("SELECT COALESCE(SUM(pr.paymentAmount), 0) FROM PaymentRecord pr WHERE pr.invoice.id = :invoiceId AND pr.paymentStatus = 'PENDING'")
    BigDecimal calculateTotalPendingAmount(@Param("invoiceId") Long invoiceId);

    /**
     * ตรวจสอบว่า Invoice ได้รับการชำระครบแล้วหรือไม่
     */
    @Query("SELECT CASE WHEN (SELECT COALESCE(SUM(pr.paymentAmount), 0) FROM PaymentRecord pr WHERE pr.invoice.id = :invoiceId AND pr.paymentStatus = 'CONFIRMED') >= pr.invoice.netAmount THEN true ELSE false END FROM PaymentRecord pr WHERE pr.invoice.id = :invoiceId")
    Boolean isInvoiceFullyPaid(@Param("invoiceId") Long invoiceId);

    /**
     * หา Payment Records ที่มี Transaction Reference
     */
    List<PaymentRecord> findByTransactionReferenceContainingIgnoreCaseOrderByPaymentDateDesc(String transactionReference);

    /**
     * ลบ Payment Records ทั้งหมดของ Invoice
     */
    void deleteByInvoiceId(Long invoiceId);
}