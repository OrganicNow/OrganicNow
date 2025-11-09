package com.organicnow.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount; // จำนวนเงินที่ชำระ

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod; // วิธีการชำระเงิน

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus; // สถานะการชำระ

    @Column(name = "payment_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Bangkok")
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
    private LocalDateTime paymentDate; // วันที่ชำระ

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference; // เลขที่อ้างอิง (เช่น เลขที่โอนเงิน)

    @Column(name = "notes", length = 1000)
    private String notes; // หมายเหตุเพิ่มเติม

    @Column(name = "recorded_by", length = 100)
    private String recordedBy; // ผู้บันทึกการชำระ

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Bangkok")
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Bangkok")
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
    private LocalDateTime updatedAt;

    // ===== Enums =====

    public enum PaymentMethod {
        CASH("เงินสด"),
        BANK_TRANSFER("โอนเงินผ่านธนาคาร"), 
        MOBILE_BANKING("Mobile Banking"),
        CHEQUE("เช็ค"),
        CREDIT_CARD("บัตรเครดิต"),
        QR_CODE("QR Code Payment"),
        OTHER("อื่นๆ");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        PENDING("รอการยืนยัน"),
        CONFIRMED("ยืนยันแล้ว"),
        REJECTED("ปฏิเสธ"),
        CANCELLED("ยกเลิก");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}