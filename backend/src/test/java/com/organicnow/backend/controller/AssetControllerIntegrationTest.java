//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.model.Asset;
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.repository.*;
//import com.organicnow.backend.service.NotificationServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest(properties = "spring.jackson.serialization.FAIL_ON_EMPTY_BEANS=false")
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class AssetControllerIntegrationTest {
//
//    @MockBean
//    private NotificationServiceImpl notificationService;
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("organicnow_test")
//            .withUsername("testuser")
//            .withPassword("testpass");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private AssetRepository assetRepository;
//    @Autowired private AssetGroupRepository assetGroupRepository;
//    @Autowired private RoomAssetRepository roomAssetRepository;
//    @Autowired private MaintenanceScheduleRepository maintenanceScheduleRepository;
//    @Autowired private NotificationRepository notificationRepository;
//
//    private AssetGroup group;
//
//    @BeforeEach
//    void setup() {
//        notificationRepository.deleteAll();
//        maintenanceScheduleRepository.deleteAll();
//        roomAssetRepository.deleteAll();
//        assetRepository.deleteAll();
//        assetGroupRepository.deleteAll();
//
//        group = new AssetGroup();
//        group.setAssetGroupName("Furniture");
//        assetGroupRepository.save(group);
//
//        Asset chair = new Asset();
//        chair.setAssetName("Chair");
//        chair.setAssetGroup(group);
//        chair.setStatus("available");
//        assetRepository.save(chair);
//
//        Asset table = new Asset();
//        table.setAssetName("Table");
//        table.setAssetGroup(group);
//        table.setStatus("available");
//        assetRepository.save(table);
//    }
//
//    // ✅ 1. /assets/all
//    @Test
//    void testGetAllAssets_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/assets/all")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result", hasSize(2)));
//    }
//
//    // ✅ 2. /assets/available
//    @Test
//    void testGetAvailableAssets_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/assets/available")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result[*].assetName", containsInAnyOrder("Chair", "Table")));
//    }
//
//    // ✅ 3. /assets/{roomId}
//    @Test
//    void testGetAssetsByRoomId_ShouldReturnEmpty() throws Exception {
//        mockMvc.perform(get("/assets/{roomId}", 999L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result", hasSize(0)));
//    }
//
//    // ✅ 4. POST /assets/bulk
//    @Test
//    void testCreateBulkAssets_ShouldCreateMultipleAssets() throws Exception {
//        mockMvc.perform(post("/assets/bulk")
//                        .param("assetGroupId", group.getId().toString())
//                        .param("name", "Desk")
//                        .param("qty", "3")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result", greaterThanOrEqualTo(3)));
//
//        long count = assetRepository.findAll().stream()
//                .filter(a -> a.getAssetName().contains("Desk"))
//                .count();
//        assert count == 3;
//    }
//
//    // ✅ 5. PATCH /assets/{id}/status
//    @Test
//    void testUpdateAssetStatus_ShouldChangeStatus() throws Exception {
//        Asset asset = assetRepository.findAll().get(0);
//        String newStatus = "maintenance";
//
//        mockMvc.perform(patch("/assets/{id}/status", asset.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"status\":\"" + newStatus + "\"}"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(newStatus))
//                .andExpect(jsonPath("$.assetName").value(asset.getAssetName()));
//
//        Asset updated = assetRepository.findById(asset.getId()).orElseThrow();
//        assert updated.getStatus().equals(newStatus);
//    }
//
//    // ✅ 6. DELETE /assets/{id}
//    @Test
//    void testSoftDeleteAsset_ShouldMarkAsDeleted() throws Exception {
//        Asset asset = assetRepository.findAll().get(0);
//        Long id = asset.getId();
//
//        mockMvc.perform(delete("/assets/{id}", id))
//                .andExpect(status().isNoContent());
//
//        Asset deleted = assetRepository.findById(id).orElse(null);
//        assert deleted != null;
//        assert deleted.getStatus().equalsIgnoreCase("deleted");
//    }
//
//    // ✅ 7. POST /assets/create (แก้ JSON format แล้ว)
//    @Test
//    void testCreateSingleAsset_ShouldReturnSuccess() throws Exception {
//        String json = String.format(
//                "{\"assetGroup\":{\"id\":%d},\"assetName\":\"Lamp\",\"status\":\"available\"}",
//                group.getId()
//        );
//
//        mockMvc.perform(post("/assets/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.assetName").value("Lamp"))
//                .andExpect(jsonPath("$.status").value("available"));
//
//        boolean exists = assetRepository.findAll().stream()
//                .anyMatch(a -> a.getAssetName().equals("Lamp"));
//        assert exists;
//    }
//
//}
