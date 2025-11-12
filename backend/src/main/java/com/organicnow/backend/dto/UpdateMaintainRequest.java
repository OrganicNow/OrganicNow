package com.organicnow.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMaintainRequest {

    private Integer targetType;          // nullable
    private Long roomId;                 // nullable
    private String roomNumber;           // nullable
    private Long roomAssetId;            // nullable

    private Integer issueCategory;       // nullable
    private String issueTitle;           // nullable
    private String issueDescription;     // nullable

    private LocalDateTime scheduledDate; // nullable
    private LocalDateTime finishDate;    // nullable

    // ✅ เพิ่มฟิลด์ใหม่
    private String maintainType;         // nullable - ประเภทการซ่อม
    private String technicianName;       // nullable - ชื่อช่าง
    private String technicianPhone;      // nullable - เบอร์โทรช่าง
    private String workImageUrl;         // nullable - URL รูปภาพหลักฐานการทำงาน
}
