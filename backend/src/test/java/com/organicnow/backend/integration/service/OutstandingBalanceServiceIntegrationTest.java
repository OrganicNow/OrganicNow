package com.organicnow.backend.integration.service;

import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.OutstandingBalanceService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OutstandingBalanceServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private OutstandingBalanceService outstandingBalanceService;

    @Autowired private ContractRepository contractRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRecordRepository paymentRecordRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private AssetGroupRepository assetGroupRepository;
    @Autowired private RoomAssetRepository roomAssetRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PackagePlanRepository packagePlanRepository;
    @Autowired private ContractTypeRepository contractTypeRepository;

    private Room room;
    private Contract contract;

    @BeforeEach
    void setup() {

        // 🏠 Room
        room = roomRepository.save(
                Room.builder()
                        .roomNumber("A201")
                        .roomFloor(2)
                        .roomSize(20)
                        .build()
        );

        // 👤 Tenant
        Tenant tenant = Tenant.builder()
                .firstName("Alice")
                .lastName("Test")
                .phoneNumber("0901112222")
                .email("alice@example.com")
                .nationalId("1234567890123")
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);

        // 📦 ContractType + PackagePlan
        ContractType ct = contractTypeRepository.save(
                ContractType.builder()
                        .name("Monthly")
                        .duration(1)
                        .build()
        );

        PackagePlan plan = packagePlanRepository.save(
                PackagePlan.builder()
                        .contractType(ct)
                        .price(BigDecimal.valueOf(2000))
                        .roomSize(1)
                        .isActive(1)
                        .build()
        );

        // 📄 Contract
        contract = contractRepository.save(
                Contract.builder()
                        .room(room)
                        .tenant(savedTenant)
                        .packagePlan(plan)
                        .signDate(LocalDateTime.now().minusDays(30))
                        .startDate(LocalDateTime.now().minusDays(30))
                        .endDate(LocalDateTime.now().plusDays(30))
                        .status(1)
                        .rentAmountSnapshot(new BigDecimal("4000"))
                        .deposit(BigDecimal.ZERO)
                        .build()
        );

        // 🛏 AssetGroup (Addon fee = 300)
        AssetGroup group = assetGroupRepository.save(
                AssetGroup.builder()
                        .assetGroupName("Extra Bed")
                        .monthlyAddonFee(new BigDecimal("300"))
                        .freeReplacement(false)
                        .build()
        );

        // 🛏 Asset
        Asset asset = assetRepository.save(
                Asset.builder()
                        .assetName("Extra Bed 01")
                        .assetGroup(group)
                        .status("in_use")
                        .build()
        );

        // 🔗 RoomAsset
        roomAssetRepository.save(
                RoomAsset.builder()
                        .asset(asset)
                        .room(room)
                        .build()
        );
    }

    // -------------------------------------------------------------
    @Test
    void testCalculateOutstandingBalance_withAddonFeeAndUnpaidInvoice() {

        // 🧾 Invoice: 4000 + 100 + 200 + addon(300) = 4600
        Invoice invoice = Invoice.builder()
                .contact(contract)
                .createDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30))
                .invoiceStatus(0)
                .requestedRent(4000)
                .requestedWater(100)
                .requestedElectricity(200)
                .requestedFloor(2)
                .requestedRoom("A201")
                .subTotal(4600)            // ✔ รวม addon fee แล้ว
                .netAmount(4600)
                .remainingBalance(4600)
                .penaltyTotal(0)
                .build();

        invoiceRepository.saveAndFlush(invoice);

        Integer result = outstandingBalanceService.calculateOutstandingBalance(contract.getId());

        Assertions.assertThat(result).isEqualTo(4600);
    }

    // -------------------------------------------------------------
    @Test
    void testRecordPayment_updatesInvoiceCorrectly() {

        Invoice invoice = Invoice.builder()
                .contact(contract)
                .createDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30))
                .invoiceStatus(0)
                .requestedRent(4000)
                .requestedWater(300)
                .requestedElectricity(0)
                .requestedFloor(2)
                .requestedRoom("A201")
                .subTotal(4300)
                .penaltyTotal(0)
                .netAmount(4300)
                .remainingBalance(4300)
                .build();

        invoiceRepository.saveAndFlush(invoice);

        outstandingBalanceService.recordPayment(
                invoice.getId(),
                new BigDecimal("1500"),
                PaymentRecord.PaymentMethod.CASH,
                "Test partial"
        );

        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();

        Assertions.assertThat(updated.getPaidAmount()).isEqualTo(1500);
        Assertions.assertThat(updated.getRemainingBalance()).isEqualTo(4300 - 1500);
        Assertions.assertThat(updated.getInvoiceStatus()).isEqualTo(0);
    }

    // -------------------------------------------------------------
    @Test
    void testCreateInvoiceWithOutstandingBalance() {

        // ใบเก่าที่ค้างชำระ
        invoiceRepository.saveAndFlush(
                Invoice.builder()
                        .contact(contract)
                        .createDate(LocalDateTime.now().minusDays(10))
                        .dueDate(LocalDateTime.now().minusDays(5))
                        .invoiceStatus(0)
                        .requestedRent(3000)
                        .requestedWater(0)
                        .requestedElectricity(0)

                        .subTotal(3000)
                        .netAmount(3000)
                        .remainingBalance(3000)
                        .penaltyTotal(0)
                        .build()
        );


        Invoice newInvoice = outstandingBalanceService.createInvoiceWithOutstandingBalance(
                contract.getId(),
                2000
        );

        Assertions.assertThat(newInvoice.getPreviousBalance()).isEqualTo(3000);
        Assertions.assertThat(newInvoice.getNetAmount()).isEqualTo(5000);
        Assertions.assertThat(newInvoice.getRemainingBalance()).isEqualTo(5000);
    }

    // -------------------------------------------------------------
    @Test
    void testGetOutstandingBalanceSummary() {

        invoiceRepository.saveAndFlush(
                Invoice.builder()
                        .contact(contract)
                        .createDate(LocalDateTime.now().minusDays(20))
                        .dueDate(LocalDateTime.now().minusDays(5))
                        .invoiceStatus(0)
                        .penaltyTotal(100)
                        .remainingBalance(2000)
                        .build()
        );

        OutstandingBalanceService.OutstandingBalanceSummary summary =
                outstandingBalanceService.getOutstandingBalanceSummary(contract.getId());

        Assertions.assertThat(summary.getTotalOutstanding()).isEqualTo(2000);
        Assertions.assertThat(summary.getTotalPenalty()).isEqualTo(100);
        Assertions.assertThat(summary.getOverdueCount()).isEqualTo(1);
        Assertions.assertThat(summary.getTotalInvoices()).isEqualTo(1);
    }
}
