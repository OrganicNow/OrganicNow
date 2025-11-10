package com.organicnow.backend.service;

import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service สำหรับจัดการยอดค้างชำระ (Outstanding Balance)
 * รองรับการแบ่งจ่ายและการสะสมยอดค้างไปยังใบแจ้งหนี้ถัดไป
 */
@Service
@Transactional
public class OutstandingBalanceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ContractRepository contractRepository;

    public OutstandingBalanceService(
            InvoiceRepository invoiceRepository,
            PaymentRecordRepository paymentRecordRepository,
            ContractRepository contractRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.contractRepository = contractRepository;
    }

    /**
     * คำนวณยอดค้างของ Contract จากใบแจ้งหนี้ที่ยังไม่ได้ชำระครบ
     */
    public Integer calculateOutstandingBalance(Long contractId) {
        System.out.println("🔍 calculateOutstandingBalance called for Contract ID: " + contractId);
        
        List<Invoice> unpaidInvoices = invoiceRepository.findByContact_IdAndInvoiceStatusOrderByCreateDateAsc(contractId, 0);
        System.out.println("📋 Found " + unpaidInvoices.size() + " unpaid invoices for Contract ID: " + contractId);
        
        int totalOutstanding = 0;
        for (Invoice invoice : unpaidInvoices) {
            System.out.println("🧾 Processing Invoice ID: " + invoice.getId() + 
                             " (Created: " + invoice.getCreateDate() + "), SubTotal: " + invoice.getSubTotal());
            
            // 🔧 คำนวณยอดคงเหลือจริง - ใช้ totalReceived แทน totalPaid
            BigDecimal totalReceived = paymentRecordRepository.calculateTotalReceivedAmount(invoice.getId());
            int receivedAmount = totalReceived != null ? totalReceived.intValue() : 0;
            System.out.println("💰 Invoice ID: " + invoice.getId() + " - Received Amount: " + receivedAmount + " บาท");
            
            // คำนวณยอดคงเหลือ - ใช้ subTotal + penalty ไม่ใช่ netAmount ที่บันทึกไว้ 🔧
            int subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : 0;
            int penaltyTotal = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
            int actualNetAmount = subTotal + penaltyTotal; // คำนวณใหม่
            int remaining = actualNetAmount - receivedAmount;
            
            System.out.println("📊 Invoice ID: " + invoice.getId() + 
                             " - SubTotal: " + subTotal + ", Penalty: " + penaltyTotal + 
                             ", ActualNet: " + actualNetAmount + ", Received: " + receivedAmount + 
                             ", Remaining: " + remaining + " บาท");
            
            if (remaining > 0) {
                totalOutstanding += remaining;
                
                // อัพเดท remainingBalance - แต่ไม่บันทึกลงฐานข้อมูลเพื่อไม่ให้กระทบการทำงานปัจจุบัน
                System.out.println("➕ Adding " + remaining + " to outstanding total");
            } else if (remaining <= 0 && invoice.getInvoiceStatus() == 0) {
                // ชำระครบแล้ว อัพเดทสถานะ
                invoice.setInvoiceStatus(1); // ชำระแล้ว
                invoice.setPayDate(LocalDateTime.now());
                invoiceRepository.save(invoice);
                System.out.println("✅ Invoice ID: " + invoice.getId() + " marked as paid");
            }
        }
        
        System.out.println("🎯 Total Outstanding Balance for Contract ID " + contractId + ": " + totalOutstanding + " บาท");
        return totalOutstanding;
    }

    /**
     * สร้างใบแจ้งหนี้ใหม่พร้อมยอดค้างจากเดือนก่อน
     */
    public Invoice createInvoiceWithOutstandingBalance(Long contractId, Integer currentMonthCharges) {
        // คำนวณยอดค้างจากใบแจ้งหนี้ก่อนหน้า
        Integer outstandingBalance = calculateOutstandingBalance(contractId);
        
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        // สร้างใบแจ้งหนี้ใหม่
        Invoice newInvoice = Invoice.builder()
                .contact(contract)
                .createDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30)) // กำหนดชำระ 30 วัน
                .invoiceStatus(0) // ยังไม่ชำระ
                .subTotal(currentMonthCharges)
                .penaltyTotal(0)
                .previousBalance(outstandingBalance) // ยอดค้างจากเดือนก่อน
                .netAmount(currentMonthCharges + outstandingBalance) // รวมยอดปัจจุบัน + ยอดค้าง
                .paidAmount(0)
                .remainingBalance(currentMonthCharges + outstandingBalance)
                .build();

        return invoiceRepository.save(newInvoice);
    }

    /**
     * บันทึกการชำระเงินและอัพเดทยอดค้าง
     */
    public PaymentRecord recordPayment(Long invoiceId, BigDecimal paymentAmount, 
                                     PaymentRecord.PaymentMethod paymentMethod, String notes) {
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        // สร้าง PaymentRecord
        PaymentRecord payment = PaymentRecord.builder()
                .invoice(invoice)
                .paymentAmount(paymentAmount)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentRecord.PaymentStatus.CONFIRMED)
                .paymentDate(LocalDateTime.now())
                .notes(notes)
                .recordedBy("SYSTEM") // หรือดึงจาก authentication
                .build();

        payment = paymentRecordRepository.save(payment);

        // อัพเดทยอดในใบแจ้งหนี้
        updateInvoiceAfterPayment(invoice);

        return payment;
    }

    /**
     * อัพเดทยอดในใบแจ้งหนี้หลังจากมีการชำระเงิน
     */
    private void updateInvoiceAfterPayment(Invoice invoice) {
        // คำนวณยอดที่ชำระแล้วทั้งหมด
        BigDecimal totalPaid = paymentRecordRepository.findTotalPaidAmountByInvoiceId(invoice.getId());
        int paidAmount = totalPaid != null ? totalPaid.intValue() : 0;

        // อัพเดท paidAmount
        invoice.setPaidAmount(paidAmount);

        // คำนวณยอดคงเหลือ
        int netAmount = invoice.getNetAmount() != null ? invoice.getNetAmount() : 0;
        int remaining = netAmount - paidAmount;
        invoice.setRemainingBalance(Math.max(0, remaining));

        // อัพเดทสถานะ
        if (remaining <= 0) {
            invoice.setInvoiceStatus(1); // ชำระแล้ว
            invoice.setPayDate(LocalDateTime.now());
        } else {
            invoice.setInvoiceStatus(0); // ยังไม่ชำระครบ
        }

        invoiceRepository.save(invoice);
    }

    /**
     * ดึงรายการใบแจ้งหนี้ที่มีการค้างชำระของ Contract
     */
    public List<Invoice> getOutstandingInvoices(Long contractId) {
        return invoiceRepository.findByContact_IdAndRemainingBalanceGreaterThanOrderByCreateDateAsc(contractId, 0);
    }

    /**
     * ดึงสรุปยอดค้างชำระของ Contract
     */
    public OutstandingBalanceSummary getOutstandingBalanceSummary(Long contractId) {
        List<Invoice> outstandingInvoices = getOutstandingInvoices(contractId);
        
        int totalOutstanding = 0;
        int totalPenalty = 0;
        int overdueCount = 0;
        
        for (Invoice invoice : outstandingInvoices) {
            int remaining = invoice.getRemainingBalance() != null ? invoice.getRemainingBalance() : 0;
            totalOutstanding += remaining;
            
            int penalty = invoice.getPenaltyTotal() != null ? invoice.getPenaltyTotal() : 0;
            totalPenalty += penalty;
            
            if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDateTime.now())) {
                overdueCount++;
            }
        }
        
        return new OutstandingBalanceSummary(totalOutstanding, totalPenalty, overdueCount, outstandingInvoices.size());
    }

    /**
     * DTO สำหรับสรุปยอดค้างชำระ
     */
    public static class OutstandingBalanceSummary {
        private final int totalOutstanding;
        private final int totalPenalty;
        private final int overdueCount;
        private final int totalInvoices;

        public OutstandingBalanceSummary(int totalOutstanding, int totalPenalty, int overdueCount, int totalInvoices) {
            this.totalOutstanding = totalOutstanding;
            this.totalPenalty = totalPenalty;
            this.overdueCount = overdueCount;
            this.totalInvoices = totalInvoices;
        }

        // Getters
        public int getTotalOutstanding() { return totalOutstanding; }
        public int getTotalPenalty() { return totalPenalty; }
        public int getOverdueCount() { return overdueCount; }
        public int getTotalInvoices() { return totalInvoices; }
    }
}