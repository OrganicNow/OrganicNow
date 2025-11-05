package com.organicnow.backend.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UtilityUsageDto {
    private String roomNumber;
    private Integer waterUsage;
    private Integer electricityUsage;
    private String billingMonth; // Format: YYYY-MM
    private Integer waterRate;   // Rate per unit
    private Integer electricityRate; // Rate per unit
}