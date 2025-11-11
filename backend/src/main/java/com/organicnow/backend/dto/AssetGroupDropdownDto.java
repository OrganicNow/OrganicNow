package com.organicnow.backend.dto;

import lombok.*;
import java.math.BigDecimal;        // ✅ เพิ่มบรรทัดนี้
import java.time.LocalDateTime;     // ✅ และอันนี้ด้วย (ถ้ายังไม่มี)


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetGroupDropdownDto {
    private Long id;
    private String name;
    private Integer threshold = 5;
    private BigDecimal monthlyAddonFee;
    private BigDecimal oneTimeDamageFee;
    private Boolean freeReplacement;
    private LocalDateTime updatedAt;
}
