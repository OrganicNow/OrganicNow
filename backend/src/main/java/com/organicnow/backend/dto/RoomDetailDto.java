package com.organicnow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailDto {
    private Long roomId;
    private String roomNumber;
    private String roomSize; // ✅ ใช้ String เพื่อให้ตอบกลับชื่อเช่น "Studio"
    private int roomFloor;
    private String status;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String contractTypeName;
    private LocalDateTime signDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AssetDto> assets;
    private List<RequestDto> requests;

    // ✅ Constructor เดิม (ไม่มี roomSize) — ใช้กับ JPQL ได้เหมือนเดิม
    public RoomDetailDto(Long roomId, String roomNumber, int roomFloor, String status,
                         String firstName, String lastName, String phoneNumber, String email,
                         String contractTypeName, LocalDateTime signDate, LocalDateTime startDate,
                         LocalDateTime endDate) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomFloor = roomFloor;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.contractTypeName = contractTypeName;
        this.signDate = signDate;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
