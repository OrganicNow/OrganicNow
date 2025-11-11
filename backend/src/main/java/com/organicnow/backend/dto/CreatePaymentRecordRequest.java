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
public class CreatePaymentRecordRequest {
    
    private Long invoiceId;
    private BigDecimal paymentAmount;
    private PaymentRecord.PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private String transactionReference;
    private String notes;
    private String recordedBy;
}