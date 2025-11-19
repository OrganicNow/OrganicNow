package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.controller.AssetController.UpdateStatusReq;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;



@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetControllerIntegrationTest {

    @TestConfiguration
    static class JacksonHibernateConfig {
        @Bean
        public Hibernate6Module hibernate6Module() {
            return new Hibernate6Module();
        }
    }
    // ----------------------------------------------------------
    // PostgreSQL Testcontainers
    // ----------------------------------------------------------
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("organicnow_test")
                    .withUsername("test")
                    .withPassword("test");

    static { postgres.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    // ----------------------------------------------------------
    // Autowired dependencies
    // ----------------------------------------------------------
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Asset related
    @Autowired AssetRepository assetRepository;
    @Autowired AssetGroupRepository assetGroupRepository;
    @Autowired RoomAssetRepository roomAssetRepository;

    // Room related
    @Autowired RoomRepository roomRepository;

    // Maintenance
    @Autowired MaintenanceNotificationSkipRepository maintenanceNotificationSkipRepository;
    @Autowired MaintenanceScheduleRepository maintenanceScheduleRepository;

    // Asset Event
    @Autowired AssetEventRepository assetEventRepository;

    // Contract related
    @Autowired ContractRepository contractRepository;
    @Autowired ContractFileRepository contractFileRepository;
    @Autowired TenantRepository tenantRepository;

    // Invoice & Payment
    @Autowired InvoiceRepository invoiceRepository;
    @Autowired PaymentRecordRepository paymentRecordRepository;
    @Autowired PaymentProofRepository paymentProofRepository;

    // Package & ContractType
    @Autowired PackagePlanRepository packagePlanRepository;
    @Autowired ContractTypeRepository contractTypeRepository;

    // Maintain (งานซ่อม)
    @Autowired MaintainRepository maintainRepository;

    // Admin (safe)
    @Autowired AdminRepository adminRepository;

    @PersistenceContext EntityManager em;

    // ----------------------------------------------------------
    // Clean Database Before Each Test
    // ----------------------------------------------------------
    @BeforeEach
    void resetDb() {

        // --- Maintenance ---
        maintenanceNotificationSkipRepository.deleteAll();
        maintenanceScheduleRepository.deleteAll();

        // --- Asset chain ---
        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();
        assetGroupRepository.deleteAll();

        // --- Payments & Billing ---
        paymentProofRepository.deleteAll();
        paymentRecordRepository.deleteAll();
        invoiceRepository.deleteAll();

        // --- Maintenance Requests ---
        maintainRepository.deleteAll();

        // --- Contract chain ---
        contractFileRepository.deleteAll();
        contractRepository.deleteAll();
        tenantRepository.deleteAll();

        // --- Package & Types ---
        packagePlanRepository.deleteAll();
        contractTypeRepository.deleteAll();

        // --- Rooms ---
        roomRepository.deleteAll();

        // --- Admin ---
        adminRepository.deleteAll();
    }

    // ----------------------------------------------------------
    // Helper: Create AssetGroup easily
    // ----------------------------------------------------------
    AssetGroup createGroup(String name) {
        return assetGroupRepository.save(
                AssetGroup.builder()
                        .assetGroupName(name)
                        .monthlyAddonFee(BigDecimal.ZERO)
                        .oneTimeDamageFee(BigDecimal.ZERO)
                        .freeReplacement(true)
                        .build()
        );
    }

    // ----------------------------------------------------------
    // 1) GET /assets/all
    // ----------------------------------------------------------
    @Test
    @Order(1)
    void getAllAssets_shouldReturnList() throws Exception {

        AssetGroup g = createGroup("Table");
        assetRepository.save(Asset.builder()
                .assetGroup(g)
                .assetName("T-001")
                .status("available")
                .build()
        );

        mockMvc.perform(get("/assets/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.length()").value(1));

    }

    // ----------------------------------------------------------
    // 2) GET /assets/available
    // ----------------------------------------------------------
    @Test
    @Order(2)
    void getAvailableAssets_shouldReturnAvailableOnly() throws Exception {

        AssetGroup g = createGroup("Chair");

        assetRepository.save(Asset.builder()
                .assetGroup(g)
                .assetName("C-001")
                .status("available")
                .build()
        );

        assetRepository.save(Asset.builder()
                .assetGroup(g)
                .assetName("C-002")
                .status("in_use")
                .build()
        );

        mockMvc.perform(get("/assets/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));

    }

    // ----------------------------------------------------------
    // 3) POST /assets/create
    // ----------------------------------------------------------
    @Test
    @Order(3)
    void createAsset_shouldReturn201() throws Exception {

        AssetGroup g = createGroup("Electronic");

        Asset payload = Asset.builder()
                .assetGroup(g)
                .assetName("TV-001")
                .status("available")
                .build();

        mockMvc.perform(post("/assets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        assertThat(assetRepository.count()).isEqualTo(1);
    }

    // ----------------------------------------------------------
    // 4) PUT /assets/update/{id}
    // ----------------------------------------------------------
    @Test
    @Order(4)
    void updateAsset_shouldModifyData() throws Exception {

        AssetGroup g = createGroup("Fan");
        Asset a = assetRepository.save(
                Asset.builder().assetGroup(g).assetName("FAN-001").status("available").build()
        );

        Asset update = Asset.builder()
                .assetGroup(g)
                .assetName("FAN-UPDATED")
                .status("in_use")
                .build();

        mockMvc.perform(put("/assets/update/" + a.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        assertThat(assetRepository.findById(a.getId()).orElseThrow().getAssetName())
                .isEqualTo("FAN-UPDATED");
    }

    // ----------------------------------------------------------
    // 5) PATCH /assets/{id}/status
    // ----------------------------------------------------------
    @Test
    @Order(5)
    void updateStatus_shouldChangeAssetStatus() throws Exception {

        AssetGroup g = createGroup("Light");
        Asset a = assetRepository.save(
                Asset.builder().assetGroup(g).assetName("L-001").status("available").build()
        );

        UpdateStatusReq req = new UpdateStatusReq();
        req.setStatus("maintenance");

        mockMvc.perform(patch("/assets/" + a.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        assertThat(assetRepository.findById(a.getId()).orElseThrow().getStatus())
                .isEqualTo("maintenance");
    }

    // ----------------------------------------------------------
    // 6) DELETE /assets/delete/{id}
    // ----------------------------------------------------------
    @Test
    @Order(6)
    void softDelete_shouldMarkAssetAsDeleted() throws Exception {

        AssetGroup g = createGroup("Bed");
        Asset a = assetRepository.save(
                Asset.builder().assetGroup(g).assetName("B-001").status("available").build()
        );

        mockMvc.perform(delete("/assets/delete/" + a.getId()))
                .andExpect(status().isNoContent());

        assertThat(assetRepository.findById(a.getId()).orElseThrow().getStatus())
                .isEqualTo("deleted");
    }

    // ----------------------------------------------------------
    // 7) POST /assets/bulk
    // ----------------------------------------------------------
    @Test
    @Order(7)
    void bulkCreate_shouldCreateCorrectQuantity() throws Exception {

        AssetGroup g = createGroup("Desk");

        mockMvc.perform(post("/assets/bulk")
                        .param("assetGroupId", g.getId().toString())
                        .param("name", "DESK")
                        .param("qty", "3"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value(3));


        assertThat(assetRepository.findByAssetGroupId(g.getId())).hasSize(3);
    }

    // ----------------------------------------------------------
    // 8) GET /assets/{roomId}
    // ----------------------------------------------------------
    @Test
    @Order(8)
    void getAssetsByRoom_shouldReturnAssignedAssets() throws Exception {

        AssetGroup g = createGroup("Drawer");

        Room room = roomRepository.save(
                Room.builder().roomNumber("A101").roomFloor(1).roomSize(1).build()
        );

        Asset a = assetRepository.save(
                Asset.builder().assetGroup(g).assetName("D-001").status("in_use").build()
        );

        roomAssetRepository.save(RoomAsset.builder()
                .room(room)
                .asset(a)
                .build());

        mockMvc.perform(get("/assets/" + room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));

    }

    // ----------------------------------------------------------
    // 9) GET /assets (listAllAssets)
    // ----------------------------------------------------------
    @Test
    @Order(9)
    void listAllAssets_shouldReturnAllAssets() throws Exception {

        AssetGroup g = createGroup("Misc");
        assetRepository.save(
                Asset.builder().assetGroup(g).assetName("M-001").status("available").build()
        );

        mockMvc.perform(get("/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));

    }
}
