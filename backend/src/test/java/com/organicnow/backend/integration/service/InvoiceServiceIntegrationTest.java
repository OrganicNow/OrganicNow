package com.organicnow.backend.integration.service;

import com.organicnow.backend.dto.CreateInvoiceRequest;
import com.organicnow.backend.dto.InvoiceDto;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.InvoiceService;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Testcontainers
@SpringBootTest
@Transactional        // ⭐ ให้ EntityManager มี transaction เสมอ
class InvoiceServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private InvoiceService invoiceService;
    @Autowired private RoomRepository roomRepo;
    @Autowired private TenantRepository tenantRepo;
    @Autowired private PackagePlanRepository packagePlanRepo;
    @Autowired private ContractRepository contractRepo;
    @Autowired private InvoiceRepository invoiceRepo;

    @Autowired private EntityManager em; // ⭐ ใช้ persist ContractType

    private Contract savedContract;

    @BeforeEach
    void setup() {

        // ⭐ unique room number per test
        String roomNum = "A101-" + System.nanoTime();

        Room room = roomRepo.save(
                Room.builder()
                        .roomNumber(roomNum)
                        .roomFloor(1)
                        .roomSize(0)
                        .build()
        );

        Tenant tenant = tenantRepo.save(
                Tenant.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .nationalId("1234567890123")
                        .email("john@example.com")
                        .phoneNumber("0900000000")
                        .build()
        );

        // ⭐ ContractType persist ด้วย transaction ที่ได้จาก @Transactional
        ContractType type = ContractType.builder()
                .name("Monthly")
                .duration(1)
                .build();

        em.persist(type);
        em.flush();

        PackagePlan plan = packagePlanRepo.save(
                PackagePlan.builder()
                        .contractType(type)
                        .price(new BigDecimal("5000"))
                        .isActive(1)
                        .roomSize(0)
                        .build()
        );

        savedContract = contractRepo.save(
                Contract.builder()
                        .room(room)
                        .tenant(tenant)
                        .packagePlan(plan)
                        .signDate(LocalDateTime.now().minusMonths(1))
                        .startDate(LocalDateTime.now().minusMonths(1))
                        .status(1)
                        .deposit(BigDecimal.ZERO)
                        .rentAmountSnapshot(new BigDecimal("5000"))
                        .build()
        );
    }

    // ================= TESTS ==================

    @Test
    void testCreateInvoice() {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setContractId(savedContract.getId());
        req.setRentAmount(5000);
        req.setWaterUnit(5);
        req.setWaterRate(30);
        req.setElectricityUnit(10);
        req.setElectricityRate(8);

        InvoiceDto dto = invoiceService.createInvoice(req);

        Assertions.assertThat(dto.getWater()).isEqualTo(150);
        Assertions.assertThat(dto.getElectricity()).isEqualTo(80);
    }

    @Test
    void testGetInvoiceById() {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setContractId(savedContract.getId());
        req.setRentAmount(5000);

        InvoiceDto created = invoiceService.createInvoice(req);

        var found = invoiceService.getInvoiceById(created.getId());

        Assertions.assertThat(found).isPresent();
    }

    @Test
    void testDeleteInvoice() {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setContractId(savedContract.getId());
        req.setRentAmount(5000);

        InvoiceDto created = invoiceService.createInvoice(req);

        invoiceService.deleteInvoice(created.getId());

        Assertions.assertThat(invoiceRepo.findById(created.getId())).isNotPresent();
    }
}
