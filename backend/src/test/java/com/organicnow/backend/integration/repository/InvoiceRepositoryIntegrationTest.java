package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.*;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class InvoiceRepositoryIntegrationTest {

    // ----------------------------------------------------------------------
    //  Testcontainers
    // ----------------------------------------------------------------------
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    // ----------------------------------------------------------------------
    //  Repository Autowire
    // ----------------------------------------------------------------------
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;
    @Autowired private MaintainRepository maintainRepository;
    @Autowired private RoomAssetRepository roomAssetRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private AssetGroupRepository assetGroupRepository;
    @Autowired private MaintenanceScheduleRepository maintenanceScheduleRepository;

    // ----------------------------------------------------------------------
    //  Clean DB — ใช้โครงแบบที่คุณส่งมาให้ตรงทั้งหมด
    // ----------------------------------------------------------------------
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
        assetGroupRepository.deleteAll();

        // 6) ตารางอื่น ๆ
        packagePlanRepository.deleteAll();
        contractTypeRepository.deleteAll();
        tenantRepository.deleteAll();

        // 7) room
        roomRepository.deleteAll();
    }


    // ----------------------------------------------------------------------
    //  Helper Methods
    // ----------------------------------------------------------------------

    private Room insertRoom(String number, int floor) {
        Room r = new Room();
        r.setRoomNumber(number);
        r.setRoomFloor(floor);
        r.setRoomSize(0);
        return roomRepository.save(r);
    }

    private Tenant insertTenant(String natId, String email) {
        Tenant t = new Tenant();
        t.setFirstName("T");
        t.setLastName("A");
        t.setNationalId(natId);
        t.setPhoneNumber("0000");
        t.setEmail(email);
        return tenantRepository.save(t);
    }

    private ContractType insertContractType(String name) {
        ContractType ct = new ContractType();
        ct.setName(name);
        ct.setDuration(12);
        return contractTypeRepository.save(ct);
    }

    private PackagePlan insertPlan(ContractType ct) {
        PackagePlan p = new PackagePlan();
        p.setContractType(ct);
        p.setRoomSize(0);
        p.setIsActive(1);
        p.setPrice(BigDecimal.valueOf(5000));
        return packagePlanRepository.save(p);
    }

    private Contract insertContract(Room room, Tenant tenant, PackagePlan plan,
                                    LocalDateTime start, LocalDateTime end, int status) {
        Contract c = new Contract();
        c.setRoom(room);
        c.setTenant(tenant);
        c.setPackagePlan(plan);
        c.setSignDate(start.minusDays(2));
        c.setStartDate(start);
        c.setEndDate(end);
        c.setStatus(status);
        c.setDeposit(BigDecimal.valueOf(2000));
        c.setRentAmountSnapshot(BigDecimal.valueOf(5000));
        return contractRepository.save(c);
    }

    private Invoice insertInvoice(Contract c, int status, LocalDateTime createDate) {
        Invoice i = new Invoice();
        i.setContact(c);
        i.setCreateDate(createDate);
        i.setDueDate(createDate.plusDays(7));
        i.setInvoiceStatus(status);
        i.setRequestedRent(5000);
        i.setSubTotal(5000);
        i.setNetAmount(5000);
        return invoiceRepository.save(i);
    }


    // ----------------------------------------------------------------------
    //  TEST CASES
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("save(): บันทึก Invoice สำเร็จ")
    void save_success() {
        Room r = insertRoom("101", 1);
        Tenant t = insertTenant("X1", "x@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(r, t, p, LocalDateTime.now(), LocalDateTime.now().plusDays(10), 1);

        Invoice inv = insertInvoice(c, 1, LocalDateTime.now());

        assertThat(inv.getId()).isNotNull();
        assertThat(inv.getContact().getId()).isEqualTo(c.getId());
    }


    @Test
    @DisplayName("findByContact_IdOrderByIdDesc(): คืน invoice เรียงใหม่ไปเก่า")
    void findByContractId_desc() {
        Room r = insertRoom("101", 1);
        Tenant t = insertTenant("X2", "y@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(r, t, p, LocalDateTime.now(), LocalDateTime.now().plusDays(30), 1);

        Invoice i1 = insertInvoice(c, 1, LocalDateTime.now().minusDays(2));
        Invoice i2 = insertInvoice(c, 1, LocalDateTime.now().minusDays(1));

        List<Invoice> list = invoiceRepository.findByContact_IdOrderByIdDesc(c.getId());

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(i2.getId());
    }


    @Test
    @DisplayName("findByContact_Id(): คืน invoice ตาม contract")
    void findByContractId_simple() {
        Room r = insertRoom("102", 1);
        Tenant t = insertTenant("Nxxx", "a@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(r, t, p, LocalDateTime.now(), LocalDateTime.now().plusDays(30), 1);

        insertInvoice(c, 1, LocalDateTime.now());

        List<Invoice> list = invoiceRepository.findByContact_Id(c.getId());

        assertThat(list).hasSize(1);
    }


    @Test
    @DisplayName("findByContractAndDateRange(): หาตามช่วงวันที่สำเร็จ")
    void findByDateRange_success() {
        Room r = insertRoom("103", 1);
        Tenant t = insertTenant("DATE1", "d@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(r, t, p,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 12, 1, 0, 0),
                1);

        Invoice inv = insertInvoice(c, 1, LocalDateTime.of(2025, 2, 10, 0, 0));

        Optional<Invoice> found =
                invoiceRepository.findByContractAndDateRange(
                        c.getId(),
                        LocalDateTime.of(2025, 2, 1, 0, 0),
                        LocalDateTime.of(2025, 2, 28, 0, 0)
                );

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(inv.getId());
    }


    @Test
    @DisplayName("findOverdueInvoicesByContract(): หาใบแจ้งหนี้ค้างชำระ")
    void findOverdue_success() {
        Room r = insertRoom("201", 2);
        Tenant t = insertTenant("Z001", "z@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);

        Contract c = insertContract(
                r, t, p,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().plusDays(30),
                1
        );

        Invoice overdue = insertInvoice(c, 0, LocalDateTime.now().minusDays(20));
        overdue.setDueDate(LocalDateTime.now().minusDays(10));
        invoiceRepository.save(overdue);

        List<Invoice> list =
                invoiceRepository.findOverdueInvoicesByContract(c.getId(), LocalDateTime.now());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getInvoiceStatus()).isEqualTo(0);
    }


    // ----------------------------------------------------------------------
    // Native Query Tests (สรุปการเงิน / Usage)
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("countFinanceLast12Months(): Query dashboard ทำงาน")
    void dashboardFinance_success() {
        Room r = insertRoom("301", 3);
        Tenant t = insertTenant("F1", "fin@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(r, t, p, LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(1), 1);

        insertInvoice(c, 1, LocalDateTime.now().minusDays(10)); // on time

        List<Object[]> data = invoiceRepository.countFinanceLast12Months();

        assertThat(data).isNotEmpty();
    }


    @Test
    @DisplayName("findRoomUsageSummary(): room usage summary ทำงาน")
    void roomUsageSummary_success() {
        Room r = insertRoom("501", 5);
        Tenant t = insertTenant("US1", "us@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(r, t, p, LocalDateTime.now(), LocalDateTime.now().plusMonths(1), 1);

        Invoice i = insertInvoice(c, 1, LocalDateTime.now());
        i.setRequestedWaterUnit(5);
        i.setRequestedElectricityUnit(10);
        invoiceRepository.save(i);

        List<Object[]> result = invoiceRepository.findRoomUsageSummary();

        assertThat(result).isNotEmpty();
    }


    @Test
    @DisplayName("findUsageByMonth(): ตรวจ usage report รายเดือน")
    void usageByMonth_success() {
        Room r = insertRoom("601", 6);
        Tenant t = insertTenant("UM1", "um@email.com");
        ContractType ct = insertContractType("Monthly");
        PackagePlan p = insertPlan(ct);
        Contract c = insertContract(
                r, t, p,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 12, 31, 0, 0),
                1);

        Invoice inv = insertInvoice(c, 1, LocalDateTime.of(2025, 3, 10, 0, 0));
        inv.setRequestedRent(6000);
        inv.setRequestedWater(10);
        inv.setRequestedWaterUnit(5);
        inv.setRequestedElectricity(7);
        inv.setRequestedElectricityUnit(3);
        invoiceRepository.save(inv);

        List<Object[]> result = invoiceRepository.findUsageByMonth("2025-03");

        assertThat(result).isNotEmpty();
    }

}
