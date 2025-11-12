package com.organicnow.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintain")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Maintain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintain_id")
    private Long id; // Maintain_id

    /** 0 = ซ่อมของในห้อง | 1 = ซ่อมห้อง */
    @Min(0) @Max(1)
    @Column(name = "target_type", nullable = false)
    private Integer targetType; // Target_type

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_maintain_room"))
    private Room room; // room_id -> Room

    // อาจว่างได้ กรณีเป็นงานซ่อมห้องทั้งห้อง
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_asset_id", foreignKey = @ForeignKey(name = "fk_maintain_room_asset"))
    private RoomAsset roomAsset; // room_asset_id -> RoomAsset

    /** ประเภทปัญหา: 0 โครงสร้าง, 1 ไฟฟ้า, 2 ประปา, 3 เครื่องใช้/เฟอร์นิเจอร์, 4 ความปลอดภัย, 5 อื่นๆ */
    @Min(0) @Max(5)
    @Column(name = "issue_category", nullable = false)
    private Integer issueCategory; // issue_category

    @NotBlank
    @Size(max = 200)
    @Column(name = "issue_title", nullable = false, length = 200)
    private String issueTitle; // issue_title

    @Column(name = "issue_description", columnDefinition = "TEXT")
    private String issueDescription; // issue_description

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate; // วันที่แจ้ง/เปิดงาน

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate; // วันที่นัดเข้าทำ

    @Column(name = "finish_date")
    private LocalDateTime finishDate; // วันที่เสร็จจริง/ปิดงาน

    @Size(max = 50)
    @Column(name = "maintain_type", length = 50)
    private String maintainType; // ประเภทการซ่อม เช่น Fix, Shift, Replace, Maintenance

    @Size(max = 100)
    @Column(name = "technician_name", length = 100)
    private String technicianName; // ชื่อช่าง

    @Size(max = 20)
    @Column(name = "technician_phone", length = 20)
    private String technicianPhone; // เบอร์โทรช่าง

    @Column(name = "work_image_url", length = 500)
    private String workImageUrl; // URL รูปภาพหลักฐานการทำงาน
}
