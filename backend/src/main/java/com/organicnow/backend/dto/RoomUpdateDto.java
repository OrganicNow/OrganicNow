package com.organicnow.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomUpdateDto {

    private Integer roomFloor;   // ชั้นของห้อง เช่น 3
    private String roomNumber;   // หมายเลขห้อง เช่น "302"
    private String status;       // "available" หรือ "occupied"

    /**
     * roomSize รองรับทั้ง:
     *  - String: "Studio", "Superior", "Deluxe"
     *  - หรือเลข: "0", "1", "2"
     *
     * ใน service จะมี logic แปลงค่าให้อัตโนมัติ
     */
    private String roomSize;
}
