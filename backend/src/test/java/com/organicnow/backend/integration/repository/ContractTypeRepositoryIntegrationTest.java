package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.ContractType;
import com.organicnow.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ContractTypeRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private MaintainRepository maintainRepository;
    @Autowired private RoomAssetRepository roomAssetRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private AssetGroupRepository assetGroupRepository;
    @Autowired private MaintenanceScheduleRepository maintenanceScheduleRepository;


    @BeforeEach
    void clean() {

        // 1) ตารางที่อ้าง contract
        invoiceRepository.deleteAll();

        // 2) ตารางลูกของ room
        roomAssetRepository.deleteAll();
        maintainRepository.deleteAll();

        // ⭐⭐ 3) ตารางลูกของ asset_group — สำคัญที่สุด
        maintenanceScheduleRepository.deleteAll();

        // 4) contract
        contractRepository.deleteAll();

        // 5) asset และ asset_group
        assetRepository.deleteAll();
        assetGroupRepository.deleteAll();  // ตอนนี้จะไม่ error แล้ว

        // 6) ตารางอื่น ๆ
        packagePlanRepository.deleteAll();
        contractTypeRepository.deleteAll();
        tenantRepository.deleteAll();

        // 7) room
        roomRepository.deleteAll();
    }


    // Helper
    private ContractType insert(String name, int duration) {
        ContractType c = new ContractType();
        c.setName(name);
        c.setDuration(duration);
        return contractTypeRepository.save(c);
    }

    @Test
    @DisplayName("save(): บันทึก ContractType สำเร็จ")
    void save_success() {
        ContractType type = insert("Monthly", 12);

        assertThat(type.getId()).isNotNull();
        assertThat(type.getName()).isEqualTo("Monthly");
        assertThat(type.getDuration()).isEqualTo(12);
    }

    @Test
    @DisplayName("findById(): หา ContractType ได้สำเร็จ")
    void findById_success() {
        ContractType saved = insert("Monthly", 12);

        Optional<ContractType> found = contractTypeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Monthly");
    }

    @Test
    @DisplayName("findAll(): คืนข้อมูลทั้งหมด")
    void findAll_success() {
        insert("Monthly", 12);
        insert("Yearly", 24);

        assertThat(contractTypeRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("findByName(): หาได้เมื่อชื่อถูกต้อง")
    void findByName_success() {
        insert("Weekly", 1);

        Optional<ContractType> found = contractTypeRepository.findByName("Weekly");

        assertThat(found).isPresent();
        assertThat(found.get().getDuration()).isEqualTo(1);
    }

    @Test
    @DisplayName("findByName(): คืน Empty เมื่อไม่พบ")
    void findByName_notFound() {
        Optional<ContractType> found = contractTypeRepository.findByName("NotExist");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("update(): อัปเดตข้อมูลสำเร็จ")
    void update_success() {
        ContractType type = insert("Monthly", 12);

        type.setDuration(24);
        contractTypeRepository.save(type);

        ContractType updated = contractTypeRepository.findById(type.getId()).orElseThrow();
        assertThat(updated.getDuration()).isEqualTo(24);
    }

    @Test
    @DisplayName("delete(): ลบข้อมูลสำเร็จ")
    void delete_success() {
        ContractType type = insert("Monthly", 12);

        contractTypeRepository.delete(type);

        assertThat(contractTypeRepository.findById(type.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unique Constraint: ชื่อซ้ำควร INSERT ได้เพราะไม่มี UNIQUE constraint")
    void uniqueName_constraint() {
        insert("Monthly", 12);

        ContractType duplicate = new ContractType();
        duplicate.setName("Monthly");
        duplicate.setDuration(10);

        ContractType saved = contractTypeRepository.saveAndFlush(duplicate);

        assertThat(saved.getId()).isNotNull();
    }

}
