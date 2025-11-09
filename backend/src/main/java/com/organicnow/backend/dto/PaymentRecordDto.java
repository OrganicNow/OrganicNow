package com.organicnow.backend.dto;

import com.organicnow.backend.model.PaymentRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecordDto {
    
    private Long id;
    private Long invoiceId;
    private BigDecimal paymentAmount;
    private PaymentRecord.PaymentMethod paymentMethod;
    private String paymentMethodDisplay;
    private PaymentRecord.PaymentStatus paymentStatus;
    private String paymentStatusDisplay;
    private LocalDateTime paymentDate;
    private String transactionReference;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // หลักฐานการชำระเงิน
    private List<PaymentProofDto> paymentProofs;
    
    public static PaymentRecordDto fromEntity(PaymentRecord entity) {
        return PaymentRecordDto.builder()
                .id(entity.getId())
                .invoiceId(entity.getInvoice().getId())
                .paymentAmount(entity.getPaymentAmount())
                .paymentMethod(entity.getPaymentMethod())
                .paymentMethodDisplay(entity.getPaymentMethod().getDisplayName())
                .paymentStatus(entity.getPaymentStatus())
                .paymentStatusDisplay(entity.getPaymentStatus().getDisplayName())
                .paymentDate(entity.getPaymentDate())
                .transactionReference(entity.getTransactionReference())
                .notes(entity.getNotes())
                .recordedBy(entity.getRecordedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}