package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetGroupControllerIntegrationTest {

    // -------------------------------
    // PostgreSQL Testcontainer
    // -------------------------------
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("organicnow_test")
                    .withUsername("test")
                    .withPassword("test");

    static { postgres.start(); }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    // -------------------------------
    // Injected components
    // -------------------------------
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired AssetGroupRepository assetGroupRepository;
    @Autowired AssetRepository assetRepository;

    @Autowired MaintenanceScheduleRepository maintenanceScheduleRepository;
    @Autowired MaintenanceNotificationSkipRepository maintenanceNotificationSkipRepository;

    @Autowired AssetEventRepository assetEventRepository;
    @Autowired RoomAssetRepository roomAssetRepository;

    // -------------------------------
    // Reset DB before each test
    // -------------------------------
    @BeforeEach
    void reset() {

        // ต้องลบ Maintenance ก่อน (มี FK ไปยัง asset_group)
        maintenanceNotificationSkipRepository.deleteAll();
        maintenanceScheduleRepository.deleteAll();

        // ลบ asset chain ทั้งหมด
        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        // ลบ asset_group สุดท้าย
        assetGroupRepository.deleteAll();
    }

    // -------------------------------
    // 1) POST /asset-group/create
    // -------------------------------
    @Test
    @Order(1)
    void createAssetGroup_shouldReturn201() throws Exception {

        AssetGroup payload = AssetGroup.builder()
                .assetGroupName("Furniture")
                .monthlyAddonFee(BigDecimal.ZERO)
                .oneTimeDamageFee(BigDecimal.ZERO)
                .freeReplacement(true)
                .build();

        mockMvc.perform(post("/asset-group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetGroupName").value("Furniture"));
    }

    // -------------------------------
    // 2) PUT /asset-group/update/{id}
    // -------------------------------
    @Test
    @Order(2)
    void updateAssetGroup_shouldUpdateValues() throws Exception {

        AssetGroup saved = assetGroupRepository.save(AssetGroup.builder()
                .assetGroupName("Old")
                .monthlyAddonFee(BigDecimal.ZERO)
                .oneTimeDamageFee(BigDecimal.ZERO)
                .freeReplacement(true)
                .build());

        AssetGroup updated = AssetGroup.builder()
                .assetGroupName("New")
                .monthlyAddonFee(BigDecimal.valueOf(500))
                .oneTimeDamageFee(BigDecimal.valueOf(100))
                .freeReplacement(false)
                .build();

        mockMvc.perform(put("/asset-group/update/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetGroupName").value("New"));
    }

    // -------------------------------
    // 3) DELETE /asset-group/delete/{id}
    // -------------------------------
    @Test
    @Order(3)
    void deleteAssetGroup_shouldDeleteAssetsAlso() throws Exception {

        AssetGroup g = assetGroupRepository.save(
                AssetGroup.builder()
                        .assetGroupName("Electronic")
                        .monthlyAddonFee(BigDecimal.ZERO)
                        .oneTimeDamageFee(BigDecimal.ZERO)
                        .freeReplacement(true)
                        .build()
        );

        assetRepository.save(Asset.builder().assetGroup(g).assetName("TV-001").status("available").build());
        assetRepository.save(Asset.builder().assetGroup(g).assetName("TV-002").status("available").build());

        mockMvc.perform(delete("/asset-group/delete/" + g.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedAssets").value(2));

        assertThat(assetGroupRepository.count()).isZero();
        assertThat(assetRepository.count()).isZero();
    }

    // -------------------------------
    // 4) GET /asset-group/list
    // -------------------------------
    @Test
    @Order(4)
    void getAllGroups_shouldReturnList() throws Exception {

        assetGroupRepository.save(AssetGroup.builder().assetGroupName("Chair").build());
        assetGroupRepository.save(AssetGroup.builder().assetGroupName("Table").build());

        mockMvc.perform(get("/asset-group/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
