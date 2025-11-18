package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractControllerIntegrationTest {

    // -------------------------------------------------------
    // Testcontainers
    // -------------------------------------------------------
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("organicnow_test")
                    .withUsername("test")
                    .withPassword("test");

    static { postgres.start(); }

    @DynamicPropertySource
    static void setupProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    // -------------------------------------------------------
    // Repositories
    // -------------------------------------------------------
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired RoomRepository roomRepository;
    @Autowired TenantRepository tenantRepository;

    @Autowired ContractRepository contractRepository;
    @Autowired ContractTypeRepository contractTypeRepository;
    @Autowired PackagePlanRepository packagePlanRepository;

    @Autowired ContractFileRepository contractFileRepository;

    // Billing
    @Autowired InvoiceRepository invoiceRepository;
    @Autowired PaymentRecordRepository paymentRecordRepository;
    @Autowired PaymentProofRepository paymentProofRepository;

    // Maintenance
    @Autowired MaintainRepository maintainRepository;
    @Autowired MaintenanceScheduleRepository maintenanceScheduleRepository;
    @Autowired MaintenanceNotificationSkipRepository maintenanceNotificationSkipRepository;

    // Asset
    @Autowired RoomAssetRepository roomAssetRepository;
    @Autowired AssetRepository assetRepository;
    @Autowired AssetGroupRepository assetGroupRepository;

    // Admin
    @Autowired AdminRepository adminRepository;

    // -------------------------------------------------------
    // Clean DB — FIXED ORDER
    // -------------------------------------------------------
    @BeforeEach
    void cleanDatabase() {

        // --- 1) Maintenance ---
        maintenanceNotificationSkipRepository.deleteAll();
        maintenanceScheduleRepository.deleteAll();
        maintainRepository.deleteAll();

        // --- 2) Billing ---
        paymentProofRepository.deleteAll();
        paymentRecordRepository.deleteAll();
        invoiceRepository.deleteAll();       // MUST delete before contract

        // --- 3) Contract chain ---
        contractFileRepository.deleteAll();
        contractRepository.deleteAll();      // MUST delete after invoice
        tenantRepository.deleteAll();

        // --- 4) Room-Asset chain ---
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();
        assetGroupRepository.deleteAll();

        // --- 5) Package & Contract Type ---
        packagePlanRepository.deleteAll();
        contractTypeRepository.deleteAll();

        // --- 6) Rooms ---
        roomRepository.deleteAll();

        // --- 7) Admin ---
        adminRepository.deleteAll();
    }

    // -------------------------------------------------------
    // Helper: Create full contract
    // -------------------------------------------------------
    Contract createFullContract(Integer floor, String roomNumber) {

        Room room = roomRepository.save(
                Room.builder()
                        .roomNumber(roomNumber)
                        .roomFloor(floor)
                        .roomSize(30)
                        .build()
        );

        Tenant tenant = tenantRepository.save(
                Tenant.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .phoneNumber("0912345678")
                        .email("john@example.com")
                        .nationalId("1234567890123") // ⭐ FIX: Required for @NotBlank
                        .build()
        );


        ContractType type = contractTypeRepository.save(
                ContractType.builder()
                        .name("Monthly")
                        .duration(12)
                        .build()
        );

        PackagePlan plan = packagePlanRepository.save(
                PackagePlan.builder()
                        .contractType(type)
                        .price(BigDecimal.valueOf(5000))
                        .isActive(1)
                        .roomSize(30)
                        .build()
        );

        return contractRepository.save(
                Contract.builder()
                        .room(room)
                        .tenant(tenant)
                        .packagePlan(plan)
                        .status(1)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusMonths(12))
                        .deposit(BigDecimal.valueOf(3000))
                        .rentAmountSnapshot(BigDecimal.valueOf(5000))
                        .build()
        );
    }

    // -------------------------------------------------------
    // TESTS
    // -------------------------------------------------------

    @Test
    @Order(1)
    void getContractList_shouldReturnList() throws Exception {

        createFullContract(3, "301");

        mockMvc.perform(get("/contract/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Order(2)
    void getTenantList_shouldReturnTenantList() throws Exception {

        createFullContract(5, "501");

        mockMvc.perform(get("/contract/tenant/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Order(3)
    void getOccupiedRooms_shouldReturnRoomIds() throws Exception {

        Contract c = createFullContract(2, "201");

        mockMvc.perform(get("/contract/occupied-rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value(c.getRoom().getId()));
    }

    @Test
    @Order(4)
    void getContractByRoom_shouldReturnCorrectContract() throws Exception {

        Contract c = createFullContract(7, "701");

        mockMvc.perform(get("/contract/by-room")
                        .param("floor", "7")
                        .param("room", "701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId").value(c.getId()));
    }
}
