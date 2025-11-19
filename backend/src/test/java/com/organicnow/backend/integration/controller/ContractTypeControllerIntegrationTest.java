package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.model.ContractType;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractTypeControllerIntegrationTest {

    // -------------------------------------------------------
    // Testcontainers
    // -------------------------------------------------------
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("organicnow_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    // -------------------------------------------------------
    // Injected Beans
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
    // Helper
    // -------------------------------------------------------
    ContractType createType(String name, int duration) {
        return contractTypeRepository.save(
                ContractType.builder()
                        .name(name)
                        .duration(duration)
                        .build()
        );
    }

    // -------------------------------------------------------
    // 1) GET /contract-types
    // -------------------------------------------------------
    @Test
    @Order(1)
    void getAllContractTypes_shouldReturnList() throws Exception {

        createType("Monthly", 12);
        createType("Daily", 1);

        mockMvc.perform(get("/contract-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // -------------------------------------------------------
    // 2) GET /contract-types/{id}
    // -------------------------------------------------------
    @Test
    @Order(2)
    void getContractTypeById_shouldReturnCorrectType() throws Exception {

        ContractType type = createType("Weekly", 4);

        mockMvc.perform(get("/contract-types/" + type.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contract_name").value("Weekly"))
                .andExpect(jsonPath("$.duration").value(4));
    }

    // -------------------------------------------------------
    // 3) POST /contract-types
    // -------------------------------------------------------
    @Test
    @Order(3)
    void createContractType_shouldReturn201() throws Exception {

        ContractType payload = ContractType.builder()
                .name("Yearly")
                .duration(12)
                .build();

        mockMvc.perform(post("/contract-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contract_name").value("Yearly"));

        assertThat(contractTypeRepository.count()).isEqualTo(1);
    }

    // -------------------------------------------------------
    // 4) DELETE /contract-types/{id}
    // -------------------------------------------------------
    @Test
    @Order(4)
    void deleteContractType_shouldDeleteSuccessfully() throws Exception {

        ContractType type = createType("Temp", 2);

        mockMvc.perform(delete("/contract-types/" + type.getId()))
                .andExpect(status().isNoContent());

        assertThat(contractTypeRepository.existsById(type.getId())).isFalse();
    }
}
