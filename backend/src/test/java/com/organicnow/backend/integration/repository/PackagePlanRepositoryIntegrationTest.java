package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class PackagePlanRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private MaintainRepository maintainRepository;
    @Autowired private RoomAssetRepository roomAssetRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private AssetGroupRepository assetGroupRepository;
    @Autowired private MaintenanceScheduleRepository maintenanceScheduleRepository;

    // ---------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------

    private Room insertRoom(String num, int floor) {
        String uniqueRoomNumber = num + "_" + System.currentTimeMillis();  // Unique room number with timestamp
        Room r = new Room();
        r.setRoomNumber(uniqueRoomNumber);
        r.setRoomFloor(floor);
        r.setRoomSize(0);
        return roomRepository.save(r);
    }

    private Tenant insertTenant(String natId, String emailPrefix) {
        Tenant t = new Tenant();
        t.setFirstName("Test");
        t.setLastName("Tenant");
        t.setNationalId(natId);
        t.setPhoneNumber("0000000000");
        t.setEmail(emailPrefix + "@email.com");
        return tenantRepository.save(t);
    }

    private ContractType insertContractType(String name) {
        ContractType ct = new ContractType();
        ct.setName(name);
        ct.setDuration(12);
        return contractTypeRepository.save(ct);
    }

    private PackagePlan insertPlan(ContractType type, int roomSize) {
        PackagePlan p = new PackagePlan();
        p.setContractType(type);
        p.setIsActive(1);
        p.setRoomSize(roomSize);
        p.setPrice(new BigDecimal("5000"));
        return packagePlanRepository.save(p);
    }

    private Contract insertContract(Room room, Tenant tenant, PackagePlan plan,
                                    LocalDateTime start, LocalDateTime end, int status) {
        Contract c = new Contract();
        c.setRoom(room);
        c.setTenant(tenant);
        c.setPackagePlan(plan);
        c.setSignDate(start.minusDays(3));
        c.setStartDate(start);
        c.setEndDate(end);
        c.setDeposit(new BigDecimal("2000"));
        c.setRentAmountSnapshot(new BigDecimal("5000"));
        c.setStatus(status);
        return contractRepository.save(c);
    }

    /**
     * Clean up data to prevent conflicts for subsequent tests.
     */
    private void clean() {
        // Delete data in proper order to avoid foreign key errors
        invoiceRepository.deleteAll();
        roomAssetRepository.deleteAll();
        maintainRepository.deleteAll();
        maintenanceScheduleRepository.deleteAll();
        contractRepository.deleteAll();
        assetRepository.deleteAll();
        assetGroupRepository.deleteAll();
        packagePlanRepository.deleteAll();
        contractTypeRepository.deleteAll();
        tenantRepository.deleteAll();
        roomRepository.deleteAll();
    }

    // ---------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------

    @Test
    @DisplayName("findTenantRows(): ดึงข้อมูล tenant row สำเร็จ")
    void findTenantRows_success() {
        clean();  // Clean database before the test

        // Insert test data
        Room room = insertRoom("101", 1);  // Unique room number
        Tenant tenant = insertTenant("999001", "t1");
        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        insertContract(
                room, tenant, plan,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(10),
                1
        );

        // When: Fetch tenant rows
        var rows = contractRepository.findTenantRows();

        // Then: Assert that the rows are returned correctly
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getFirstName()).isEqualTo("Test");
        assertThat(rows.get(0).getContractTypeId()).isEqualTo(type.getId());
    }

    @Test
    @DisplayName("findTenantRowsByTenantIds(): คืนเฉพาะ tenant ที่ระบุ")
    void findTenantRowsByTenantIds_success() {
        clean();

        Room r1 = insertRoom("101", 1);
        Room r2 = insertRoom("102", 1);

        Tenant t1 = insertTenant("N1", "a1");
        Tenant t2 = insertTenant("N2", "a2");

        ContractType type = insertContractType("Monthly");
        PackagePlan p = insertPlan(type, 0);

        insertContract(r1, t1, p, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), 1);
        insertContract(r2, t2, p, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), 1);

        var rows = contractRepository.findTenantRowsByTenantIds(List.of(t2.getId()));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getEmail()).contains("a2");
    }

    @Test
    @DisplayName("existsByTenant_IdAndStatusAndEndDateAfter(): ตรวจ active contract ตาม tenantId")
    void existsByTenantId_activeContract() {
        clean();

        Room room = insertRoom("101", 1);
        Tenant tenant = insertTenant("871100", "abc");
        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        insertContract(
                room, tenant, plan,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                1
        );

        boolean exists = contractRepository.existsByTenant_IdAndStatusAndEndDateAfter(
                tenant.getId(), 1, LocalDateTime.now()
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsActiveContractByRoomId(): ห้องมีสัญญา active หรือไม่")
    void existsActiveContractByRoomId_success() {
        clean();

        Room room = insertRoom("101", 1);
        Tenant tenant = insertTenant("N123456", "aa");
        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        insertContract(room, tenant, plan,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(3),
                1);

        boolean exists = contractRepository.existsActiveContractByRoomId(room.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findCurrentlyOccupiedRoomIds(): คืน room id เฉพาะที่ active")
    void findCurrentlyOccupiedRoomIds_success() {
        clean();

        Room r1 = insertRoom("101", 1);
        Room r2 = insertRoom("102", 1);

        Tenant t1 = insertTenant("R1", "x1");
        Tenant t2 = insertTenant("R2", "x2");

        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        insertContract(r1, t1, plan, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), 1);
        insertContract(r2, t2, plan, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1), 1);

        List<Long> ids = contractRepository.findCurrentlyOccupiedRoomIds();

        assertThat(ids).containsExactly(r1.getId());
    }

    @Test
    @DisplayName("updateExpiredContracts(): เปลี่ยน status ของสัญญาที่หมดอายุ")
    void updateExpiredContracts_success() {
        clean();

        Room r = insertRoom("101", 1);
        Tenant t = insertTenant("X999", "u1");
        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        insertContract(r, t, plan,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1),
                1);

        int updated = contractRepository.updateExpiredContracts();

        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("findByRoomAndPackagePlan_IdAndStatus(): หา contract ตาม room, packageId, status")
    void findByRoomAndPackagePlanId_success() {
        clean();

        Room r = insertRoom("101", 1);
        Tenant t = insertTenant("T123", "z1");
        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        Contract c = insertContract(
                r, t, plan,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                1
        );

        Optional<Contract> found =
                contractRepository.findByRoomAndPackagePlan_IdAndStatus(
                        r, plan.getId(), 1);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(c.getId());
    }

    @Test
    @DisplayName("findActiveContractByRoomId(): คืน active contract ล่าสุดของห้อง")
    void findActiveContractByRoomId_success() {
        clean();

        Room r = insertRoom("101", 1);
        Tenant t = insertTenant("90001", "q1");
        ContractType type = insertContractType("Monthly");
        PackagePlan plan = insertPlan(type, 0);

        Contract c = insertContract(
                r, t, plan,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                1
        );

        Optional<Contract> found =
                contractRepository.findActiveContractByRoomId(r.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(c.getId());
    }
}
