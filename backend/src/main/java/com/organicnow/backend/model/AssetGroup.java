package com.organicnow.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "asset_group",
        uniqueConstraints = @UniqueConstraint(name = "uk_asset_group_name", columnNames = "asset_group_name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_group_id")
    private Long id; // AssetGroup ID

    @NotBlank
    @Size(max = 100)
    @Column(name = "asset_group_name", nullable = false, length = 100)
    private String assetGroupName; // AssetGroup Name

    // 💰 ค่าบริการรายเดือน (กรณีเป็นของเสริม เช่น เตียงเพิ่ม)
    @DecimalMin(value = "0.00")
    @Column(name = "monthly_addon_fee", precision = 10, scale = 2)
    private BigDecimal monthlyAddonFee = BigDecimal.ZERO;

    // ⚙️ ค่าซ่อมหรือค่าเสียหายแบบครั้งเดียว (เช่น เก้าอี้พัง)
    @DecimalMin(value = "0.00")
    @Column(name = "one_time_damage_fee", precision = 10, scale = 2)
    private BigDecimal oneTimeDamageFee = BigDecimal.ZERO;

    // 🆓 ฟรีหรือไม่ (true = เปลี่ยนฟรี เช่น หลอดไฟ)
    @Column(name = "free_replacement")
    private Boolean freeReplacement = true;

    // 🕒 เวลาอัปเดตล่าสุด (optional — เผื่ออยาก track การเปลี่ยนราคา)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}