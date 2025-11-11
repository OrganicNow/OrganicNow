package com.organicnow.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceAddonDto {

    private Long id;
    private Long invoiceId;
    private Long assetId;
    private String assetName; // ชื่อ Asset
    private String assetGroupName; // ชื่อ Asset Group
    private String addonName; // ชื่อ Add-on
    private Integer quantity; // จำนวน
    private BigDecimal unitPrice; // ราคาต่อหน่วย
    private BigDecimal totalPrice; // ราคารวม
    private String description; // คำอธิบาย
    
    // Helper method for display
    public String getDisplayName() {
        return addonName + " (" + quantity + " รายการ)";
    }
    
    public String getFormattedTotal() {
        return totalPrice != null ? String.format("%,.2f", totalPrice) : "0.00";
    }
}