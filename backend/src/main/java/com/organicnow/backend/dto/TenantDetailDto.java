package com.organicnow.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantDetailDto {
    private Long contractId;

    // Tenant info
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nationalId;

    // Room
    private Integer floor;
    private String room;

    // Package
    private Long contractTypeId;
    private String packageName;
    private BigDecimal packagePrice;

    // Contract
    private LocalDateTime signDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;
    private BigDecimal deposit;
    private BigDecimal rentAmountSnapshot;

    // Invoice list
    private List<InvoiceDto> invoices;

    // Nested DTO สำหรับ invoice
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InvoiceDto {
        private Long invoiceId;
        private LocalDateTime createDate;
        private LocalDateTime dueDate;
        private Integer invoiceStatus;
        private Integer netAmount;
        private LocalDateTime payDate;
        private Integer payMethod;
        private Integer penaltyTotal;
        private Integer subTotal;
    }
}