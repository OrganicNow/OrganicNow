//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
//import com.organicnow.backend.service.ContractService;
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
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class ContractControllerIntegrationTest {
//
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
//    @Autowired private JdbcTemplate jdbcTemplate;
//    @Autowired private MockMvc mockMvc;
//    @Autowired private RoomRepository roomRepository;
//    @Autowired private TenantRepository tenantRepository;
//    @Autowired private PackagePlanRepository packagePlanRepository;
//    @Autowired private ContractRepository contractRepository;
//    @Autowired private ContractTypeRepository contractTypeRepository;
//    @Autowired private ContractService contractService;
//
//    @BeforeEach
//    @Transactional
//    void cleanDatabaseAndSetup() {
//        // 🧹 ล้างข้อมูลเก่าพร้อม reset id
//        jdbcTemplate.execute("""
//            TRUNCATE TABLE invoice_item, invoice, contract, tenant, room, package_plan, contract_type
//            RESTART IDENTITY CASCADE
//        """);
//
//        // ✅ สร้าง ContractType
//        ContractType contractType = ContractType.builder()
//                .name("Standard Contract")
//                .duration(12)
//                .build();
//        contractTypeRepository.save(contractType);
//
//        // ✅ สร้าง PackagePlan ที่ผูกกับ ContractType
//        PackagePlan plan = PackagePlan.builder()
//                .contractType(contractType)
//                .price(BigDecimal.valueOf(5000))
//                .isActive(1)
//                .build();
//        packagePlanRepository.save(plan);
//
//        // ✅ สร้างห้อง
//        Room r1 = roomRepository.save(Room.builder().roomNumber("101").roomFloor(1).build());
//        Room r2 = roomRepository.save(Room.builder().roomNumber("102").roomFloor(1).build());
//
//        // ✅ สร้างผู้เช่า
//        Tenant t1 = tenantRepository.save(Tenant.builder()
//                .firstName("John").lastName("Doe")
//                .phoneNumber("0812345678")
//                .email("john@example.com")
//                .nationalId("1234567890123").build());
//
//        Tenant t2 = tenantRepository.save(Tenant.builder()
//                .firstName("Jane").lastName("Smith")
//                .phoneNumber("0811111111")
//                .email("jane@example.com")
//                .nationalId("9876543210987").build());
//
//        // ✅ สร้างสัญญา
//        Contract c1 = Contract.builder()
//                .room(r1).tenant(t1).packagePlan(plan)
//                .startDate(LocalDateTime.now().minusMonths(2))
//                .endDate(LocalDateTime.now().plusMonths(2))
//                .signDate(LocalDateTime.now().minusMonths(2))
//                .status(1)
//                .deposit(BigDecimal.valueOf(2000))
//                .rentAmountSnapshot(BigDecimal.valueOf(5000))
//                .build();
//
//        Contract c2 = Contract.builder()
//                .room(r2).tenant(t2).packagePlan(plan)
//                .startDate(LocalDateTime.now().minusMonths(6))
//                .endDate(LocalDateTime.now().minusMonths(1))
//                .signDate(LocalDateTime.now().minusMonths(6))
//                .status(1)
//                .deposit(BigDecimal.valueOf(2000))
//                .rentAmountSnapshot(BigDecimal.valueOf(5000))
//                .build();
//
//        contractRepository.saveAll(List.of(c1, c2));
//    }
//
//    // ✅ 1. /contract/list
//    @Test
//    void testGetContractList_ShouldReturnTenantList() throws Exception {
//        mockMvc.perform(get("/contract/list")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", not(empty())))
//                .andExpect(jsonPath("$[0].firstName", anyOf(is("John"), is("Jane"))));
//    }
//
//    // ✅ 2. /contract/tenant/list
//    @Test
//    void testGetTenantList_ShouldReturnSameList() throws Exception {
//        mockMvc.perform(get("/contract/tenant/list")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
//                .andExpect(jsonPath("$[0].firstName", notNullValue()));
//    }
//
//    // ✅ 3. /contract/occupied-rooms
//    @Test
//    void testGetOccupiedRooms_ShouldReturnActiveRooms() throws Exception {
//        mockMvc.perform(get("/contract/occupied-rooms")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", not(empty())));
//    }
//}
