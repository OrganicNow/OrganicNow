package com.organicnow.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    // ✅ ย้าย @JoinColumn มาหลัง field id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_id",
            referencedColumnName = "contract_id",
            nullable = false,
            unique = true
    )
    private Contract contract;

    // ✅ ให้ Hibernate map ตรงแบบ binary โดยบังคับ explicit type
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "signed_pdf", nullable = true)
    private byte[] signedPdf;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}