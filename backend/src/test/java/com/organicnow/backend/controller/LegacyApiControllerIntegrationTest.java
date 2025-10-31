//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
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
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * ✅ Full Integration Test for LegacyApiController
// * ครอบคลุม Controller → Service → Repository → Model → PostgreSQL (Testcontainers)
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class LegacyApiControllerIntegrationTest {
//
//    // 🐘 PostgreSQL Testcontainer
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("organicnow_test")
//            .withUsername("testuser")
//            .withPassword("testpass");
//
//    @DynamicPropertySource
//    static void configureProps(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private JdbcTemplate jdbcTemplate;
//    @Autowired private ContractRepository contractRepository;
//    @Autowired private RoomRepository roomRepository;
//    @Autowired private TenantRepository tenantRepository;
//    @Autowired private PackagePlanRepository packagePlanRepository;
//    @Autowired private ContractTypeRepository contractTypeRepository;
//
//    @BeforeEach
//    void setup() {
//        jdbcTemplate.execute("""
//            TRUNCATE TABLE contract, room, tenant, package_plan, contract_type
//            RESTART IDENTITY CASCADE
//        """);
//
//        // ✅ Seed data: ContractType + PackagePlan
//        ContractType contractType = contractTypeRepository.save(
//                ContractType.builder().name("Standard").duration(12).build()
//        );
//
//        PackagePlan plan = packagePlanRepository.save(
//                PackagePlan.builder().contractType(contractType).price(BigDecimal.valueOf(5000)).isActive(1).build()
//        );
//
//        // ✅ Room 2 ห้อง
//        Room room1 = roomRepository.save(Room.builder().roomNumber("101").roomFloor(1).build());
//        Room room2 = roomRepository.save(Room.builder().roomNumber("102").roomFloor(1).build());
//        Room room3 = roomRepository.save(Room.builder().roomNumber("103").roomFloor(1).build());
//
//        // ✅ Tenant
//        Tenant tenant1 = tenantRepository.save(Tenant.builder()
//                .firstName("John").lastName("Doe")
//                .phoneNumber("0812345678")
//                .email("john@example.com")
//                .nationalId("1234567890123").build());
//
//        Tenant tenant2 = tenantRepository.save(Tenant.builder()
//                .firstName("Jane").lastName("Smith")
//                .phoneNumber("0811111111")
//                .email("jane@example.com")
//                .nationalId("9876543210987").build());
//
//        // ✅ Contracts: occupied room = 101, 102
//        Contract c1 = Contract.builder()
//                .room(room1).tenant(tenant1).packagePlan(plan)
//                .startDate(LocalDateTime.now().minusMonths(2))
//                .endDate(LocalDateTime.now().plusMonths(10))
//                .signDate(LocalDateTime.now().minusMonths(2))
//                .status(1)
//                .deposit(BigDecimal.valueOf(2000))
//                .rentAmountSnapshot(BigDecimal.valueOf(5000))
//                .build();
//
//        Contract c2 = Contract.builder()
//                .room(room2).tenant(tenant2).packagePlan(plan)
//                .startDate(LocalDateTime.now().minusMonths(5))
//                .endDate(LocalDateTime.now().plusMonths(6))
//                .signDate(LocalDateTime.now().minusMonths(5))
//                .status(1)
//                .deposit(BigDecimal.valueOf(2500))
//                .rentAmountSnapshot(BigDecimal.valueOf(5500))
//                .build();
//
//        contractRepository.saveAll(List.of(c1, c2));
//    }
//
//    // ---------------------------------------------------------
//    // ✅ 1. GET /contracts/occupied-rooms
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("GET /contracts/occupied-rooms → should return list of occupied room IDs")
//    void testGetOccupiedRooms_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/contracts/occupied-rooms")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[*]", containsInAnyOrder(
//                        roomRepository.findAll().get(0).getId().intValue(),
//                        roomRepository.findAll().get(1).getId().intValue()
//                )));
//    }
//
//    // ---------------------------------------------------------
//    // ✅ 2. GET /contracts/occupied-rooms → empty case
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("GET /contracts/occupied-rooms → should return empty when no active contracts")
//    void testGetOccupiedRooms_EmptyCase() throws Exception {
//        contractRepository.deleteAll(); // ❌ ไม่มีสัญญา
//
//        mockMvc.perform(get("/contracts/occupied-rooms"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(0)));
//    }
//
//    // ---------------------------------------------------------
//    // ✅ 3. GET /contracts (legacy)
//    // ---------------------------------------------------------
//    @Test
//    @DisplayName("GET /contracts → should return empty list (legacy placeholder)")
//    void testGetContractsLegacy_ShouldReturnEmpty() throws Exception {
//        mockMvc.perform(get("/contracts"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(0)));
//    }
//}
