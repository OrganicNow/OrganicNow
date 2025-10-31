package com.organicnow.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDto {

    private Long contractId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String nationalId;

    private String room;     // room number
    private Integer floor;   // room floor
    private Long roomId;     // room id

    // 🔹 package plan
    private Long packageId;

    // 🔹 contract type (ใช้ filter)
    private Long contractTypeId;
    private String contractName;

    // 🔹 วันที่
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime signDate;

    // 🔹 เงิน
    private BigDecimal deposit;
    private BigDecimal rentAmountSnapshot;  // ✅ เหลืออันเดียว

    // 🔹 สถานะ (0=หมดอายุ, 1=ใช้งาน, 2=ยังไม่เริ่ม, 3=ยกเลิก)
    private Integer status;

    // ---------- Constructor สำหรับ JPQL ----------
    public TenantDto(
            Long contractId,
            String firstName,
            String lastName,
            Integer floor,
            String room,
            Long roomId,
            Long packageId,
            Long contractTypeId,
            String contractName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String phoneNumber,
            String email,
            String nationalId,
            Integer status,
            BigDecimal rentAmountSnapshot  // ✅ เพิ่มพารามิเตอร์นี้
    ) {
        this.contractId = contractId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.floor = floor;
        this.room = room;
        this.roomId = roomId;
        this.packageId = packageId;
        this.contractTypeId = contractTypeId;
        this.contractName = contractName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.nationalId = nationalId;
        this.status = status;
        this.rentAmountSnapshot = rentAmountSnapshot; // ✅ set ค่าได้จริง
    }
}