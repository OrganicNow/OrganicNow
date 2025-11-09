package com.organicnow.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_proofs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_record_id", nullable = false)
    private PaymentRecord paymentRecord;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName; // ชื่อไฟล์เดิม

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath; // path ของไฟล์ในระบบ

    @Column(name = "file_size")
    private Long fileSize; // ขนาดไฟล์ (bytes)

    @Column(name = "content_type", length = 100)
    private String contentType; // MIME type (image/jpeg, image/png, application/pdf เป็นต้น)

    @Enumerated(EnumType.STRING)
    @Column(name = "proof_type", nullable = false)
    private ProofType proofType; // ประเภทหลักฐาน

    @Column(name = "description", length = 500)
    private String description; // คำอธิบายหลักฐาน

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy; // ผู้อัปโหลด

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Bangkok")
    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
    private LocalDateTime uploadedAt;

    // ===== Enum =====

    public enum ProofType {
        RECEIPT("ใบเสร็จรับเงิน"),
        BANK_SLIP("สลิปการโอนเงิน"),
        BANK_STATEMENT("Statement ธนาคาร"),
        CHEQUE_COPY("สำเนาเช็ค"),
        OTHER("หลักฐานอื่นๆ");

        private final String displayName;

        ProofType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}