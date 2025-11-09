package com.organicnow.backend.dto;

import com.organicnow.backend.model.PaymentProof;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProofDto {
    
    private Long id;
    private Long paymentRecordId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private PaymentProof.ProofType proofType;
    private String proofTypeDisplay;
    private String description;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    
    public String getOriginalFilename() {
        return this.fileName;
    }
    
    public String getOriginalFileName() {
        return this.fileName;
    }
    
    public String getFileType() {
        return this.contentType;
    }
    
    public static PaymentProofDto fromEntity(PaymentProof entity) {
        return PaymentProofDto.builder()
                .id(entity.getId())
                .paymentRecordId(entity.getPaymentRecord().getId())
                .fileName(entity.getFileName())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .proofType(entity.getProofType())
                .proofTypeDisplay(entity.getProofType().getDisplayName())
                .description(entity.getDescription())
                .uploadedBy(entity.getUploadedBy())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}