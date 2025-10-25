package com.organicnow.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "package_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_type_id", nullable = false)
    private ContractType contractType;

    @Min(0)
    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Integer isActive;   // 0 = ไม่ใช้งาน, 1 = ใช้งาน

    @NotNull
    @Column(name = "package_size", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer packageSize = 0;   // 0 = Small, 1 = Medium, 2 = Large
}
