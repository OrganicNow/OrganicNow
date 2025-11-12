//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.model.ContractType;
//import com.organicnow.backend.repository.ContractTypeRepository;
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
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class ContractTypeControllerIntegrationTest {
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
//    @Autowired private MockMvc mockMvc;
//    @Autowired private JdbcTemplate jdbcTemplate;
//    @Autowired private ContractTypeRepository contractTypeRepository;
//
//    @BeforeEach
//    void setup() {
//        jdbcTemplate.execute("TRUNCATE TABLE contract_type RESTART IDENTITY CASCADE");
//
//        contractTypeRepository.save(ContractType.builder()
//                .name("Standard Contract")
//                .duration(12)
//                .build());
//
//        contractTypeRepository.save(ContractType.builder()
//                .name("Short Term")
//                .duration(6)
//                .build());
//    }
//
//    // ✅ 1. GET /contract-types
//    @Test
//    void testGetAllContractTypes_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/contract-types")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[*].contract_name", containsInAnyOrder("Standard Contract", "Short Term")))
//                .andExpect(jsonPath("$[*].duration", containsInAnyOrder(12, 6)));
//    }
//
//    // ✅ 2. GET /contract-types/{id}
//    @Test
//    void testGetContractTypeById_ShouldReturnCorrectType() throws Exception {
//        ContractType first = contractTypeRepository.findAll().get(0);
//
//        mockMvc.perform(get("/contract-types/{id}", first.getId())
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(first.getId().intValue())))
//                .andExpect(jsonPath("$.contract_name", is(first.getName())))
//                .andExpect(jsonPath("$.duration", is(first.getDuration())));
//    }
//
//    // ✅ 3. POST /contract-types
//    @Test
//    void testCreateContractType_ShouldReturnCreated() throws Exception {
//        String json = """
//            {
//                "contract_name": "Long Term",
//                "duration": 24
//            }
//        """;
//
//        mockMvc.perform(post("/contract-types")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id", notNullValue()))
//                .andExpect(jsonPath("$.contract_name", is("Long Term")))
//                .andExpect(jsonPath("$.duration", is(24)));
//
//        assert contractTypeRepository.count() == 3;
//    }
//
//    // ✅ 4. DELETE /contract-types/{id}
//    @Test
//    void testDeleteContractType_ShouldRemoveEntity() throws Exception {
//        ContractType first = contractTypeRepository.findAll().get(0);
//        Long id = first.getId();
//
//        mockMvc.perform(delete("/contract-types/{id}", id))
//                .andExpect(status().isNoContent());
//
//        assert contractTypeRepository.findById(id).isEmpty();
//    }
//
//    // ✅ 5. GET /contract-types/{id} not found
//    @Test
//    void testGetContractTypeById_ShouldReturn500() throws Exception {
//        mockMvc.perform(get("/contract-types/{id}", 999L)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.message", containsString("ContractType not found")));
//    }
//}
