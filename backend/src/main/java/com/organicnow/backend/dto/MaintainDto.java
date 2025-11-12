// src/main/java/com/organicnow/backend/dto/MaintainDto.java
package com.organicnow.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintainDto {
    private Long id;
    private Integer targetType;          // 0=Asset in room, 1=Room/Building
    private Long roomId;
    private String roomNumber;
    private Integer roomFloor;
    private Long roomAssetId;            // nullable
    private Integer issueCategory;     // 0..5
    private String issueTitle;
    private String issueDescription;
    private LocalDateTime createDate;
    private LocalDateTime scheduledDate;
    private LocalDateTime finishDate;

    // ✅ เพิ่มฟิลด์ใหม่
    private String maintainType;        // ประเภทการซ่อม
    private String technicianName;      // ชื่อช่าง
    private String technicianPhone;     // เบอร์โทรช่าง
    private String workImageUrl;        // URL รูปภาพหลักฐานการทำงาน

    // ช่วยแปลงสถานะไว้ใช้บน UI
    public String getState() {
        return finishDate != null ? "Complete" : "Not Started";
    }
}
