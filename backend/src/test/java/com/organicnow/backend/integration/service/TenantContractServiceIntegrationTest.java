package com.organicnow.backend.integration.service;

import com.organicnow.backend.dto.CreateTenantContractRequest;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.TenantContractService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;

@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "scheduler.enabled=false"
})
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TenantContractServiceIntegrationTest {

    // -------------------------------------------------------
    // 🔥 Static Testcontainer — ใช้ร่วมกันทุก test
    // -------------------------------------------------------
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    // Spring จะ inject ค่า datasource ให้จาก container นี้
    @DynamicPropertySource
    static void setupProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // -------------------------------------------------------
    // Autowired beans
    // -------------------------------------------------------
    @Autowired private TenantContractService tenantContractService;

    @Autowired private TenantRepository tenantRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;

    @Autowired private javax.sql.DataSource dataSource;

    private Room room;
    private PackagePlan plan;

    // -------------------------------------------------------
    // ⚡ ล้าง DB ทุก test เพื่อให้ fresh เสมอ
    // -------------------------------------------------------
    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                DO $$ DECLARE
                    r RECORD;
                BEGIN
                    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE';
                    END LOOP;
                END $$;
            """);
        }

        // ----------------------------------------------
        // 🛠️ สร้าง Room + PackagePlan ให้พร้อมใช้
        // ----------------------------------------------
        room = roomRepository.save(
                Room.builder()
                        .roomNumber("C101")
                        .roomFloor(1)
                        .roomSize(1)
                        .build()
        );

        ContractType ct = contractTypeRepository.save(
                ContractType.builder()
                        .name("Monthly")
                        .duration(1)
                        .build()
        );

        plan = packagePlanRepository.save(
                PackagePlan.builder()
                        .contractType(ct)
                        .price(BigDecimal.valueOf(3000))
                        .roomSize(1)
                        .isActive(1)
                        .build()
        );
    }

    // -------------------------------------------------------
    @Test
    void testCreateContract_NewTenant() {

        CreateTenantContractRequest req = new CreateTenantContractRequest();
        req.setFirstName("Alice");
        req.setLastName("Test");
        req.setEmail("alice@example.com");
        req.setPhoneNumber("1111");
        req.setNationalId("1234567890");
        req.setRoomId(room.getId());
        req.setPackageId(plan.getId());
        req.setStartDate(LocalDateTime.now());
        req.setEndDate(LocalDateTime.now().plusMonths(1));
        req.setDeposit(BigDecimal.valueOf(5000));
        req.setRentAmountSnapshot(BigDecimal.valueOf(3000));

        var result = tenantContractService.create(req);

        Assertions.assertThat(result.getFirstName()).isEqualTo("Alice");
        Assertions.assertThat(result.getRoom()).isEqualTo("C101");
        Assertions.assertThat(contractRepository.findAll()).hasSize(1);
    }

    // -------------------------------------------------------
    @Test
    void testCreateContract_ExistingTenant_NoActiveContract() {

        tenantRepository.save(
                Tenant.builder()
                        .firstName("Bob")
                        .lastName("Old")
                        .nationalId("999")
                        .email("b@example.com")
                        .build()
        );

        CreateTenantContractRequest req = new CreateTenantContractRequest();
        req.setNationalId("999");
        req.setRoomId(room.getId());
        req.setPackageId(plan.getId());
        req.setStartDate(LocalDateTime.now());
        req.setEndDate(LocalDateTime.now().plusMonths(1));
        req.setDeposit(BigDecimal.valueOf(2000));
        req.setRentAmountSnapshot(BigDecimal.valueOf(3000));

        var result = tenantContractService.create(req);

        Assertions.assertThat(result.getFirstName()).isEqualTo("Bob");
        Assertions.assertThat(contractRepository.count()).isEqualTo(1);
    }

    // -------------------------------------------------------
    @Test
    void testCreateContract_Throws_WhenTenantHasActiveContract() {

        Tenant t = tenantRepository.save(
                Tenant.builder().firstName("A").lastName("B").nationalId("AAA").build()
        );

        contractRepository.save(
                Contract.builder()
                        .tenant(t)
                        .room(room)
                        .packagePlan(plan)
                        .status(1)
                        .startDate(LocalDateTime.now().minusDays(10))
                        .endDate(LocalDateTime.now().plusDays(10))
                        .build()
        );

        CreateTenantContractRequest req = new CreateTenantContractRequest();
        req.setNationalId("AAA");
        req.setRoomId(room.getId());
        req.setPackageId(plan.getId());
        req.setStartDate(LocalDateTime.now());
        req.setEndDate(LocalDateTime.now().plusMonths(1));

        Assertions.assertThatThrownBy(() -> tenantContractService.create(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("tenant_already_has_active_contract");
    }

    // -------------------------------------------------------
    @Test
    void testUpdateContract() {

        Tenant t = tenantRepository.save(
                Tenant.builder()
                        .firstName("Pre")
                        .lastName("Name")
                        .nationalId("555")
                        .build()
        );

        Contract contract = contractRepository.save(
                Contract.builder()
                        .tenant(t)
                        .room(room)
                        .packagePlan(plan)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(20))
                        .rentAmountSnapshot(BigDecimal.valueOf(3000))
                        .deposit(BigDecimal.valueOf(1000))
                        .status(1)
                        .build()
        );

        UpdateTenantContractRequest req = new UpdateTenantContractRequest();
        req.setFirstName("Updated");
        req.setLastName("User");
        req.setDeposit(BigDecimal.valueOf(999));
        req.setRentAmountSnapshot(BigDecimal.valueOf(2500));

        var dto = tenantContractService.update(contract.getId(), req);

        Assertions.assertThat(dto.getFirstName()).isEqualTo("Updated");

        Contract saved = contractRepository.findById(contract.getId()).orElseThrow();

        Assertions.assertThat(saved.getDeposit().compareTo(BigDecimal.valueOf(999))).isZero();
        Assertions.assertThat(saved.getRentAmountSnapshot().compareTo(BigDecimal.valueOf(2500))).isZero();
    }

    // -------------------------------------------------------
    @Test
    void testDeleteContract_DeletesInvoicesToo() {

        Tenant t = tenantRepository.save(
                Tenant.builder().firstName("Test").lastName("User").nationalId("777").build()
        );

        Contract c = contractRepository.save(
                Contract.builder()
                        .tenant(t)
                        .room(room)
                        .packagePlan(plan)
                        .status(1)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(20))
                        .build()
        );

        invoiceRepository.save(
                Invoice.builder()
                        .contact(c)
                        .createDate(LocalDateTime.now())
                        .dueDate(LocalDateTime.now().plusDays(5))
                        .invoiceStatus(0)
                        .netAmount(1000)
                        .remainingBalance(1000)
                        .subTotal(1000)
                        .build()
        );

        tenantContractService.delete(c.getId());

        Assertions.assertThat(contractRepository.findAll()).isEmpty();
        Assertions.assertThat(invoiceRepository.findAll()).isEmpty();
    }

    // -------------------------------------------------------
    @Test
    void testGetDetail() {

        Tenant t = tenantRepository.save(
                Tenant.builder().firstName("John").lastName("Doe").nationalId("111").build()
        );

        Contract c = contractRepository.save(
                Contract.builder()
                        .tenant(t)
                        .room(room)
                        .packagePlan(plan)
                        .status(1)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(20))
                        .rentAmountSnapshot(BigDecimal.valueOf(3000))
                        .deposit(BigDecimal.valueOf(1500))
                        .build()
        );

        invoiceRepository.save(
                Invoice.builder()
                        .contact(c)
                        .createDate(LocalDateTime.now())
                        .dueDate(LocalDateTime.now().plusDays(5))
                        .invoiceStatus(0)
                        .netAmount(1000)
                        .remainingBalance(1000)
                        .subTotal(1000)
                        .build()
        );

        var detail = tenantContractService.getDetail(c.getId());

        Assertions.assertThat(detail.getFirstName()).isEqualTo("John");
        Assertions.assertThat(detail.getInvoices()).hasSize(1);
        Assertions.assertThat(detail.getPackagePrice().compareTo(BigDecimal.valueOf(3000))).isZero();

    }

    // -------------------------------------------------------
    @Test
    void testGenerateContractPdf() {

        Tenant t = tenantRepository.save(
                Tenant.builder().firstName("PDF").lastName("User").nationalId("9999").build()
        );

        Contract c = contractRepository.save(
                Contract.builder()
                        .tenant(t)
                        .room(room)
                        .packagePlan(plan)
                        .status(1)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(20))
                        .rentAmountSnapshot(BigDecimal.valueOf(3000))
                        .deposit(BigDecimal.valueOf(1000))
                        .build()
        );

        byte[] pdf = tenantContractService.generateContractPdf(c.getId());

        Assertions.assertThat(pdf).isNotNull();
        Assertions.assertThat(pdf.length).isGreaterThan(5000);
    }
}
