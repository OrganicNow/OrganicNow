//package com.organicnow.dto;
//
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CreateInvoiceRequest {
//
//    private Long contractId; // อ้างอิงถึง contract
//    private LocalDateTime dueDate; // วันครบกำหนดชำระ
//    private Integer subTotal; // ยอดปกติ เช่น ค่าเช่า/น้ำ/ไฟ รวม
//    private Integer penaltyTotal; // ยอดปรับ (ถ้ามี)
//    private Integer netAmount; // ยอดสุทธิ
//    private String notes; // หมายเหตุเพิ่มเติม (ถ้าต้องการ)
//
//    // สำหรับการสร้าง invoice items
//    private Integer rentAmount;
//    private Integer waterUnit;
//    private Integer waterRate;
//    private Integer electricityUnit;
//    private Integer electricityRate;
//}

package com.organicnow.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateInvoiceRequest {

    // ===== แบบดั้งเดิม =====
    private Long contractId;            // อ้างอิง contract (ถ้ามี)
    private LocalDateTime dueDate;      // วันครบกำหนด
    private Integer subTotal;           // ยอดปกติ (เช่า+น้ำ+ไฟ)
    private Integer penaltyTotal;       // ยอดปรับ
    private Integer netAmount;          // ยอดสุทธิ
    private String notes;               // หมายเหตุ

    // รายการย่อยเพื่อช่วยคำนวณ (ถ้ามี)
    private Integer rentAmount;
    private Integer waterUnit;
    private Integer waterRate;
    private Integer electricityUnit;
    private Integer electricityRate;

    // ===== ส่วนขยายให้รองรับ payload จาก UI ปัจจุบัน =====
    // UI ส่งวันที่สร้างเป็น yyyy-MM-dd
    private String createDate;          // ex. "2025-02-28"
    private Integer invoiceStatus;      // 0: Incomplete, 1: Complete, 2: Cancelled

    // UI บางครั้งส่งยอดเงินน้ำ/ไฟมาโดยตรง (ไม่ใช่ unit*rate)
    private Integer water;              // ยอดค่าน้ำ (บาท)
    private Integer electricity;        // ยอดค่าไฟ (บาท)

    // นามแฝงจาก UI (elecUnit -> electricityUnit)
    private Integer elecUnit;           // ใช้แทน electricityUnit ถ้าส่งมา
    
    // ===== ส่วนใหม่: รองรับการสร้าง invoice โดยไม่ต้องมี contract =====
    private Long packageId;             // อ้างอิง package โดยตรง
    private String floor;               // ชั้น (เช่น "1", "2")
    private String room;                // ห้อง (เช่น "101", "102")
    
    // ===== ส่วนใหม่: รองรับ Outstanding Balance =====
    private Boolean includeOutstandingBalance; // รวมยอดค้างชำระหรือไม่
}
