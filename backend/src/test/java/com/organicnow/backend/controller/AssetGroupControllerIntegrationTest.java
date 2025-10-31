//package com.organicnow.backend.controller;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import com.organicnow.backend.model.Asset;
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.repository.AssetGroupRepository;
//import com.organicnow.backend.repository.AssetRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import com.organicnow.backend.config.TestExceptionHandler;
//import org.springframework.context.annotation.Import;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@Import(TestExceptionHandler.class)
//@Transactional
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class AssetGroupControllerIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("organicnow_test")
//            .withUsername("testuser")
//            .withPassword("testpass");
//
//    @DynamicPropertySource
//    static void configureDataSource(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired
//    private JdbcTemplate jdbc;
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private AssetGroupRepository assetGroupRepository;
//    @Autowired private AssetRepository assetRepository;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @BeforeEach
//    void setup() {
//        jdbc.execute("DELETE FROM notifications");
//        jdbc.execute("DELETE FROM maintenance_schedule");
//        // ✅ ลบตารางลูกก่อนเพื่อล้าง FK (ลำดับสำคัญมาก)
//        entityManager.createNativeQuery("DELETE FROM notifications").executeUpdate();
//        entityManager.createNativeQuery("DELETE FROM maintenance_schedule").executeUpdate();
//        entityManager.createNativeQuery("DELETE FROM room_asset").executeUpdate();
//        entityManager.createNativeQuery("DELETE FROM asset").executeUpdate();
//        entityManager.createNativeQuery("DELETE FROM asset_group").executeUpdate();
//
//        // ✅ เพิ่มข้อมูลเริ่มต้น
//        assetGroupRepository.save(AssetGroup.builder().assetGroupName("Furniture").build());
//        assetGroupRepository.save(AssetGroup.builder().assetGroupName("Electronics").build());
//    }
//
//    // ✅ 1. GET /asset-group/list
//    @Test
//    void testGetAllAssetGroups_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/asset-group/list")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].assetGroupName", is("Furniture")))
//                .andExpect(jsonPath("$[1].assetGroupName", is("Electronics")));
//    }
//
//    // ✅ 2. POST /asset-group/create
//    @Test
//    void testCreateAssetGroup_ShouldReturnCreatedGroup() throws Exception {
//        String json = "{\"assetGroupName\": \"Appliances\"}";
//
//        mockMvc.perform(post("/asset-group/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id", notNullValue()))
//                .andExpect(jsonPath("$.assetGroupName").value("Appliances"));
//
//        assert assetGroupRepository.existsByAssetGroupName("Appliances");
//    }
//
//    // ✅ 3. POST /asset-group/create duplicate name
//    @Test
//    void testCreateAssetGroup_DuplicateName_ShouldReturnError() throws Exception {
//        String json = "{\"assetGroupName\": \"Furniture\"}";
//
//        mockMvc.perform(post("/asset-group/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.message").value("duplicate_group_name"));
//    }
//
//    // ✅ 4. PUT /asset-group/update/{id}
//    @Test
//    void testUpdateAssetGroup_ShouldUpdateSuccessfully() throws Exception {
//        AssetGroup existing = assetGroupRepository.findAll().get(0);
//        String json = "{\"assetGroupName\": \"Updated Furniture\"}";
//
//        mockMvc.perform(put("/asset-group/update/{id}", existing.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.assetGroupName").value("Updated Furniture"));
//
//        AssetGroup updated = assetGroupRepository.findById(existing.getId()).orElseThrow();
//        assert updated.getAssetGroupName().equals("Updated Furniture");
//    }
//
//    // ✅ 5. PUT /asset-group/update/{id} duplicate name
//    @Test
//    void testUpdateAssetGroup_DuplicateName_ShouldThrowError() throws Exception {
//        AssetGroup first = assetGroupRepository.findAll().get(0);
//        AssetGroup second = assetGroupRepository.findAll().get(1);
//
//        String json = String.format("{\"assetGroupName\": \"%s\"}", first.getAssetGroupName());
//
//        mockMvc.perform(put("/asset-group/update/{id}", second.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.message").value("duplicate_group_name"));
//    }
//
//    // ✅ 6. DELETE /asset-group/delete/{id} — ไม่มี asset
//    @Test
//    void testDeleteAssetGroup_ShouldDeleteSuccessfully() throws Exception {
//        AssetGroup group = assetGroupRepository.findAll().get(0);
//        Long id = group.getId();
//
//        mockMvc.perform(delete("/asset-group/delete/{id}", id))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("deleted_group"))
//                .andExpect(jsonPath("$.deletedAssets").value(0));
//
//        assert assetGroupRepository.findById(id).isEmpty();
//    }
//
//    // ✅ 7. DELETE /asset-group/delete/{id} — มี asset อยู่
//    @Test
//    void testDeleteAssetGroup_WithAssets_ShouldDeleteGroupAndAssets() throws Exception {
//        AssetGroup group = assetGroupRepository.findAll().get(1); // Electronics
//        assetRepository.save(Asset.builder().assetName("Laptop").assetGroup(group).status("available").build());
//        assetRepository.save(Asset.builder().assetName("Monitor").assetGroup(group).status("available").build());
//
//        mockMvc.perform(delete("/asset-group/delete/{id}", group.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("deleted_group"))
//                .andExpect(jsonPath("$.deletedAssets").value(2));
//
//        assert assetGroupRepository.findById(group.getId()).isEmpty();
//        assert assetRepository.findAll().isEmpty();
//    }
//}
