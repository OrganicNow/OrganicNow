package com.organicnow.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {

    private Long id;
    private Long contractId;
    private String contractDetails; // ข้อมูลสัญญาแบบสั้น
    private LocalDateTime createDate;
    private LocalDateTime dueDate;
    private Integer invoiceStatus; // 0=ยังไม่ชำระ, 1=ชำระแล้ว, 2=ยกเลิก
    private String statusText; // แปลง status เป็นข้อความ
    private LocalDateTime payDate;
    private Integer payMethod;
    private String payMethodText; // แปลง payMethod เป็นข้อความ
    private Integer subTotal;
    private Integer penaltyTotal;
    private Integer netAmount;
    private LocalDateTime penaltyAppliedAt;
    
    // ✅ Outstanding Balance fields
    private Integer previousBalance; // ยอดค้างจากเดือนก่อน
    private Integer paidAmount; // ยอดที่ชำระแล้ว
    private Integer outstandingBalance; // ยอดคงเหลือ
    private Boolean hasOutstandingBalance; // มียอดค้างหรือไม่
    
    // Payment Information
    private List<PaymentRecordDto> paymentRecords;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalPendingAmount;
    private BigDecimal remainingAmount;

    // ข้อมูลเพิ่มเติมสำหรับ frontend
    private String firstName;
    private String lastName;
    private String nationalId;
    private String phoneNumber;
    private String email;
    private String packageName;
    private LocalDateTime signDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer floor;
    private String room;
    private Integer rent; // จาก rent_amount_snapshot
    private Integer water; // จาก invoice items
    private Integer waterUnit;
    private Integer electricity; // จาก invoice items
    private Integer electricityUnit;
    private Integer addonAmount; // 🔥 Add-on fee จาก Asset Group monthly addon
    private Integer penalty; // จาก penaltyTotal > 0 ? 1 : 0
    private LocalDateTime penaltyDate; // จาก penaltyAppliedAt

    // Status text helper
    public String getStatusText() {
        if (invoiceStatus == null) return "ไม่ระบุ";
        return switch (invoiceStatus) {
            case 0 -> "Incomplete";
            case 1 -> "Complete";
            case 2 -> "Cancelled";
            default -> "Unknown";
        };
    }

    // Pay method text helper
    public String getPayMethodText() {
        if (payMethod == null) return "ไม่ระบุ";
        return switch (payMethod) {
            case 1 -> "เงินสด";
            case 2 -> "โอนเงิน";
            case 3 -> "เช็ค";
            default -> "วิธีอื่น";
        };
    }

    // Amount getter (alias for netAmount for frontend compatibility)
    public Integer getAmount() {
        return netAmount;
    }

    public void setAmount(Integer amount) {
        this.netAmount = amount;
    }

    // Status mapping for frontend
    public String getStatus() {
        if (invoiceStatus == null) return "Unknown";
        return switch (invoiceStatus) {
            case 0 -> "Incomplete";
            case 1 -> "Complete";
            case 2 -> "Cancelled";
            default -> "Unknown";
        };
    }

    public void setStatus(String status) {
        this.invoiceStatus = switch (status) {
            case "Complete" -> 1;
            case "Incomplete" -> 0;
            case "Cancelled" -> 2;
            default -> 0;
        };
    }

    // Penalty flag
    public Integer getPenalty() {
        return (penaltyTotal != null && penaltyTotal > 0) ? 1 : 0;
    }
}
