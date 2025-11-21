package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.Contract;
import com.organicnow.backend.model.ContractFile;
import com.organicnow.backend.repository.ContractFileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional  // ให้แต่ละเทส rollback เอง ไม่ทิ้ง data ค้างใน DB
class ContractFileRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private ContractFileRepository contractFileRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ------------------------------------------------------------------------
    // Helper: สร้าง row พื้นฐานด้วย native SQL แล้ว map กลับมาเป็น Contract entity
    // ------------------------------------------------------------------------

    private Long insertRoom(String tag) {
        String shortId = UUID.randomUUID().toString().substring(0, 8);
        String roomNumber = "R" + tag + "-" + shortId; // ผลลัพธ์ยาวไม่เกิน 20

        entityManager.createNativeQuery(
                        "INSERT INTO room (room_floor, room_number, room_size) " +
                                "VALUES (1, :rn, 0)")
                .setParameter("rn", roomNumber)
                .executeUpdate();

        Number id = (Number) entityManager.createNativeQuery(
                        "SELECT room_id FROM room WHERE room_number = :rn")
                .setParameter("rn", roomNumber)
                .getSingleResult();

        return id.longValue();
    }


    private Long insertTenant(String tag) {

        String email = "t_" + tag + "@test.com";
        // ความยาวรวม ~15 chars ปลอดภัยแน่นอน

        entityManager.createNativeQuery(
                        "INSERT INTO tenant (national_id, first_name, last_name, phone_number, email) " +
                                "VALUES (:nid, 'Test', 'Tenant', '0000000000', :email)")
                .setParameter("nid", System.nanoTime()) // ให้ unique
                .setParameter("email", email)
                .executeUpdate();

        Number id = (Number) entityManager.createNativeQuery(
                        "SELECT tenant_id FROM tenant WHERE email = :email")
                .setParameter("email", email)
                .getSingleResult();

        return id.longValue();
    }


    private Long insertContractType(String tag) {
        String name = "CF-CT-" + tag + "-" + UUID.randomUUID();
        entityManager.createNativeQuery(
                        "INSERT INTO contract_type (duration, contract_name) " +
                                "VALUES (12, :name)")
                .setParameter("name", name)
                .executeUpdate();

        Number id = (Number) entityManager.createNativeQuery(
                        "SELECT contract_type_id FROM contract_type WHERE contract_name = :name")
                .setParameter("name", name)
                .getSingleResult();
        return id.longValue();
    }

    private Long insertPackagePlan(Long contractTypeId) {
        BigDecimal price = new BigDecimal("1000.00");
        int roomSize = 30;

        entityManager.createNativeQuery(
                        "INSERT INTO package_plan (is_active, price, room_size, contract_type_id) " +
                                "VALUES (1, :price, :roomSize, :ctId)")
                .setParameter("price", price)
                .setParameter("roomSize", roomSize)
                .setParameter("ctId", contractTypeId)
                .executeUpdate();

        Number id = (Number) entityManager.createNativeQuery(
                        "SELECT package_id FROM package_plan " +
                                "WHERE contract_type_id = :ctId AND price = :price AND room_size = :roomSize")
                .setParameter("ctId", contractTypeId)
                .setParameter("price", price)
                .setParameter("roomSize", roomSize)
                .getSingleResult();
        return id.longValue();
    }

    private Contract createContract(String tag) {
        Long roomId = insertRoom("R-" + tag);
        Long tenantId = insertTenant("T-" + tag);
        Long contractTypeId = insertContractType("CT-" + tag);
        Long packageId = insertPackagePlan(contractTypeId);

        // status, package_id, room_id, tenant_id เป็น NOT NULL ตาม DDL
        entityManager.createNativeQuery(
                        "INSERT INTO contract (status, package_id, room_id, tenant_id) " +
                                "VALUES (1, :pkgId, :roomId, :tenantId)")
                .setParameter("pkgId", packageId)
                .setParameter("roomId", roomId)
                .setParameter("tenantId", tenantId)
                .executeUpdate();

        Number contractIdNum = (Number) entityManager.createNativeQuery(
                        "SELECT contract_id FROM contract " +
                                "WHERE package_id = :pkgId AND room_id = :roomId AND tenant_id = :tenantId")
                .setParameter("pkgId", packageId)
                .setParameter("roomId", roomId)
                .setParameter("tenantId", tenantId)
                .getSingleResult();

        Long contractId = contractIdNum.longValue();
        return entityManager.find(Contract.class, contractId);
    }

    private ContractFile createContractFile(Contract contract) {
        ContractFile file = new ContractFile();
        file.setContract(contract);
        // uploaded_at / signed_pdf จะปล่อยให้เป็น null ก็ได้ (column ไม่บังคับ)
        return contractFileRepository.save(file);
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("findByContract(): ถ้ามี ContractFile แล้ว ต้องคืน Optional ที่มีค่า")
    void findByContract_whenFileExists_shouldReturnFile() {
        // given
        Contract contract = createContract("FIND-EXISTS");
        ContractFile savedFile = createContractFile(contract);

        // when
        Optional<ContractFile> result = contractFileRepository.findByContract(contract);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedFile.getId());
        assertThat(result.get().getContract().getId()).isEqualTo(contract.getId());
    }

    @Test
    @DisplayName("findByContract(): ถ้ายังไม่มี ContractFile ต้องคืน Optional ว่าง")
    void findByContract_whenNoFile_shouldReturnEmpty() {
        // given
        Contract contract = createContract("FIND-EMPTY");

        // when
        Optional<ContractFile> result = contractFileRepository.findByContract(contract);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByContract(): คืน true เมื่อมี ContractFile ผูกกับ Contract นั้น")
    void existsByContract_whenFileExists_shouldReturnTrue() {
        // given
        Contract contract = createContract("EXISTS-TRUE");
        createContractFile(contract);

        // when
        boolean exists = contractFileRepository.existsByContract(contract);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByContract(): คืน false เมื่อไม่มี ContractFile ผูกกับ Contract นั้น")
    void existsByContract_whenNoFile_shouldReturnFalse() {
        // given
        Contract contract = createContract("EXISTS-FALSE");

        // when
        boolean exists = contractFileRepository.existsByContract(contract);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteByContract(): ลบ ContractFile ของ Contract นั้น แต่ไม่ลบ Contract เอง")
    void deleteByContract_shouldRemoveFileButKeepContract() {
        // given
        Contract contract = createContract("DELETE");
        createContractFile(contract);

        assertThat(contractFileRepository.existsByContract(contract)).isTrue();

        // when
        contractFileRepository.deleteByContract(contract);
        entityManager.flush(); // บังคับให้ยิง delete ทันที

        // then
        assertThat(contractFileRepository.existsByContract(contract)).isFalse();

        // contract ยังต้องอยู่ในระบบ
        Contract stillThere = entityManager.find(Contract.class, contract.getId());
        assertThat(stillThere).isNotNull();
    }
}
