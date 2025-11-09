package com.organicnow.backend.dto;

import com.organicnow.backend.model.PaymentRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentRecordRequest {
    
    private BigDecimal paymentAmount;
    private PaymentRecord.PaymentMethod paymentMethod;
    private PaymentRecord.PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private String transactionReference;
    private String notes;
}