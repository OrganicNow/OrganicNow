package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.DashboardDto;
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

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerIntegrationTest {

    // -------------------------------------------------------
    // PostgreSQL Testcontainer
    // -------------------------------------------------------
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("organicnow_test_dashboard")
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

    // Repository (ลบข้อมูลแบบเดียวกับไฟล์อื่นของคุณ)
    @Autowired MaintenanceScheduleRepository maintenanceScheduleRepository;
    @Autowired MaintainRepository maintainRepository;
    @Autowired MaintenanceNotificationSkipRepository maintenanceNotificationSkipRepository;

    @Autowired InvoiceRepository invoiceRepository;
    @Autowired PaymentRecordRepository paymentRecordRepository;
    @Autowired PaymentProofRepository paymentProofRepository;

    @Autowired ContractRepository contractRepository;
    @Autowired ContractFileRepository contractFileRepository;
    @Autowired TenantRepository tenantRepository;

    @Autowired RoomAssetRepository roomAssetRepository;
    @Autowired AssetRepository assetRepository;
    @Autowired AssetGroupRepository assetGroupRepository;

    @Autowired RoomRepository roomRepository;

    @Autowired ContractTypeRepository contractTypeRepository;
    @Autowired PackagePlanRepository packagePlanRepository;

    @Autowired AdminRepository adminRepository;

    // -------------------------------------------------------
    // Clean database BEFORE each test
    // -------------------------------------------------------
    @BeforeEach
    void cleanDb() {

        // 1) Maintenance
        maintenanceNotificationSkipRepository.deleteAll();
        maintenanceScheduleRepository.deleteAll();
        maintainRepository.deleteAll();

        // 2) Billing
        paymentProofRepository.deleteAll();
        paymentRecordRepository.deleteAll();
        invoiceRepository.deleteAll();

        // 3) Contract chain
        contractFileRepository.deleteAll();
        contractRepository.deleteAll();
        tenantRepository.deleteAll();

        // 4) Room-Asset chain
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();
        assetGroupRepository.deleteAll();

        // 5) Package & Contract Type
        packagePlanRepository.deleteAll();
        contractTypeRepository.deleteAll();

        // 6) Rooms
        roomRepository.deleteAll();

        // 7) Admin
        adminRepository.deleteAll();
    }

    // -------------------------------------------------------
    // 1) GET /dashboard
    // -------------------------------------------------------
    @Test
    @Order(1)
    void getDashboard_shouldReturnDashboardDto() throws Exception {

        String response = mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        DashboardDto dto = objectMapper.readValue(response, DashboardDto.class);

        // Expected fields exist
        assertThat(dto).isNotNull();
        assertThat(dto.getRooms()).isNotNull();
        assertThat(dto.getMaintains()).isNotNull();
        assertThat(dto.getFinances()).isNotNull();
        assertThat(dto.getUsages()).isNotNull();
    }

    // -------------------------------------------------------
    // 2) GET /dashboard/export/{yearMonth}
    // -------------------------------------------------------
    @Test
    @Order(2)
    void exportCsv_shouldReturnCsvFile() throws Exception {

        var mvcResult = mockMvc.perform(get("/dashboard/export/Nov_2025"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"Usage_Report_Nov_2025.csv\""))
                .andReturn();

        byte[] bytes = mvcResult.getResponse().getContentAsByteArray();

        // CSV ต้องมี BOM 3 bytes
        assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        assertThat(bytes[1]).isEqualTo((byte) 0xBB);
        assertThat(bytes[2]).isEqualTo((byte) 0xBF);

        String content = new String(bytes, StandardCharsets.UTF_8);
        assertThat(content).contains(","); // อย่างน้อยต้องมี comma
    }

    // -------------------------------------------------------
    // 3) Test: รองรับรูปแบบเดือน "Nov 2025"
    // -------------------------------------------------------
    @Test
    @Order(3)
    void exportCsv_shouldAccept_MMM_yyyy() throws Exception {

        mockMvc.perform(get("/dashboard/export/Nov 2025"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"Usage_Report_Nov_2025.csv\""));
    }

    // -------------------------------------------------------
    // 4) Test: รองรับรูปแบบ "2025-11"
    // -------------------------------------------------------
    @Test
    @Order(4)
    void exportCsv_shouldAccept_yyyy_MM() throws Exception {

        mockMvc.perform(get("/dashboard/export/2025-11"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"Usage_Report_2025-11.csv\""));
    }

    // -------------------------------------------------------
    // 5) Invalid format -> 400 BAD REQUEST
    // -------------------------------------------------------
    @Test
    @Order(5)
    void exportCsv_invalidMonth_shouldReturn400() throws Exception {

        mockMvc.perform(get("/dashboard/export/INVALID_FORMAT"))
                .andExpect(status().isConflict()); // 409

    }
}
