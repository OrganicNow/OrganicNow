package com.organicnow.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin",
        uniqueConstraints = @UniqueConstraint(name = "uk_admin_username", columnNames = "admin_username")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id; // Admin_id

    @NotBlank
    @Size(max = 60)
    @Column(name = "admin_username", nullable = false, length = 60)
    private String adminUsername; // username ของ admin

    @NotBlank
    @Size(max = 255)
    @Column(name = "admin_password", nullable = false, length = 255)
    private String adminPassword; // password ของ admin (ภายหลังควรเก็บ hash)

    @NotNull
    @Column(name = "admin_role", nullable = false)
    private Integer adminRole = 0; // 0 = Admin ปกติ, 1 = Super Admin
}