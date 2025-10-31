//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
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
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * ✅ Integration Test for MaintainController
// * ใช้ฐานข้อมูลจริงผ่าน Testcontainers (PostgreSQL 17)
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class MaintainControllerIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("organicnow_test")
//            .withUsername("testuser")
//            .withPassword("testpass");
//
//    @DynamicPropertySource
//    static void datasource(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//    @Autowired private JdbcTemplate jdbcTemplate;
//
//    @Autowired private MaintainRepository maintainRepository;
//    @Autowired private RoomRepository roomRepository;
//    @Autowired private TenantRepository tenantRepository;
//    @Autowired private ContractRepository contractRepository;
//    @Autowired private PackagePlanRepository packagePlanRepository;
//    @Autowired private ContractTypeRepository contractTypeRepository;
//
//    private Room room;
//    private Maintain maintain;
//
//    @BeforeEach
//    void setup() {
//        jdbcTemplate.execute("""
//        TRUNCATE TABLE maintain, room, tenant, contract, package_plan, contract_type
//        RESTART IDENTITY CASCADE
//    """);
//
//        ContractType type = ContractType.builder()
//                .name("Standard Contract")
//                .duration(12)
//                .build();
//        contractTypeRepository.save(type);
//
//        PackagePlan plan = PackagePlan.builder()
//                .contractType(type)
//                .price(BigDecimal.valueOf(5000))
//                .isActive(1)
//                .build();
//        packagePlanRepository.save(plan);
//
//        Tenant tenant = Tenant.builder()
//                .firstName("John")
//                .lastName("Doe")
//                .email("john@example.com")
//                .phoneNumber("0812345678")
//                .nationalId("1234567890123") // ✅ บังคับมีค่า
//                .build();
//        tenantRepository.save(tenant);
//
//        room = Room.builder()
//                .roomNumber("101")
//                .roomFloor(1)
//                .build();
//        roomRepository.save(room);
//
//        // ✅ ใช้ฟิลด์จริงจาก Maintain.java
//        maintain = Maintain.builder()
//                .room(room)
//                .targetType(0)
//                .issueCategory(1)
//                .issueTitle("Light Bulb Broken")
//                .issueDescription("Need replacement in bathroom")
//                .createDate(LocalDateTime.now())
//                .maintainType("Fix")
//                .technicianName("Somchai")
//                .technicianPhone("0811111111")
//                .build();
//        maintainRepository.save(maintain);
//    }
//
//
//    // ✅ 1. GET /maintain/list
//    @Test
//    void testGetAllMaintains_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/maintain/list"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 2. GET /maintain/{id}
//    @Test
//    void testGetMaintainById_ShouldReturnItem() throws Exception {
//        mockMvc.perform(get("/maintain/" + maintain.getId()))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 3. POST /maintain/create
//    @Test
//    void testCreateMaintain_ShouldReturnOK() throws Exception {
//        String json = """
//        {
//          "roomId": 1,
//          "targetType": 0,
//          "issueCategory": 2,
//          "issueTitle": "Air Conditioner Broken",
//          "issueDescription": "Not cooling properly",
//          "createDate": "2025-10-16T10:00:00",
//          "maintainType": "Repair",
//          "technicianName": "Somchai",
//          "technicianPhone": "0811111111"
//        }
//    """;
//
//        mockMvc.perform(post("/maintain/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk());
//    }
//
//
//    // ✅ 4. PUT /maintain/update/{id}
//    @Test
//    void testUpdateMaintain_ShouldReturnOK() throws Exception {
//        String json = """
//            {
//              "maintainTitle": "Air Conditioner Fixed",
//              "maintainStatus": 1
//            }
//        """;
//
//        mockMvc.perform(put("/maintain/update/" + maintain.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 5. DELETE /maintain/{id}
//    @Test
//    void testDeleteMaintain_ShouldReturnOK() throws Exception {
//        mockMvc.perform(delete("/maintain/" + maintain.getId()))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 6. GET /maintain/{roomId}/requests
//    @Test
//    void testGetRequestsByRoom_ShouldReturnSuccessResponse() throws Exception {
//        mockMvc.perform(get("/maintain/" + room.getId() + "/requests"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is("success")));
//    }
//}
