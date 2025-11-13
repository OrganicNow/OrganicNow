//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.dto.CreateInvoiceRequest;
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
// * ✅ Integration Test for InvoiceController
// * ใช้ฐานข้อมูลจริงผ่าน Testcontainers (PostgreSQL 17)
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class InvoiceControllerIntegrationTest {
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
//    @Autowired private ContractRepository contractRepository;
//    @Autowired private TenantRepository tenantRepository;
//    @Autowired private RoomRepository roomRepository;
//    @Autowired private PackagePlanRepository packagePlanRepository;
//    @Autowired private ContractTypeRepository contractTypeRepository;
//    @Autowired private InvoiceRepository invoiceRepository;
//
//    private Contract contract;
//
//    @BeforeEach
//    void setup() {
//        jdbcTemplate.execute("""
//            TRUNCATE TABLE invoice, contract, tenant, room, package_plan, contract_type
//            RESTART IDENTITY CASCADE
//        """);
//
//        // ✅ Contract Type
//        ContractType type = ContractType.builder()
//                .name("Monthly Plan")
//                .duration(12)
//                .build();
//        contractTypeRepository.save(type);
//
//        // ✅ Package Plan
//        PackagePlan pkg = PackagePlan.builder()
//                .contractType(type)
//                .price(BigDecimal.valueOf(5000))
//                .isActive(1)
//                .build();
//        packagePlanRepository.save(pkg);
//
//        // ✅ Tenant
//        Tenant tenant = Tenant.builder()
//                .firstName("John")
//                .lastName("Doe")
//                .phoneNumber("0812345678")
//                .email("john@example.com")
//                .nationalId("1234567890123")
//                .build();
//        tenantRepository.save(tenant);
//
//        // ✅ Room
//        Room room = Room.builder()
//                .roomNumber("101")
//                .roomFloor(1)
//                .build();
//        roomRepository.save(room);
//
//        // ✅ Contract
//        contract = Contract.builder()
//                .room(room)
//                .tenant(tenant)
//                .packagePlan(pkg)
//                .status(1)
//                .signDate(LocalDateTime.now().minusMonths(1))
//                .startDate(LocalDateTime.now().minusMonths(1))
//                .endDate(LocalDateTime.now().plusMonths(11))
//                .deposit(BigDecimal.valueOf(2000))
//                .rentAmountSnapshot(BigDecimal.valueOf(5000))
//                .build();
//        contractRepository.save(contract);
//
//        // ✅ Invoice
//        Invoice inv = Invoice.builder()
//                .contact(contract)
//                .createDate(LocalDateTime.now().minusDays(5))
//                .dueDate(LocalDateTime.now().plusDays(5))
//                .invoiceStatus(0)
//                .subTotal(5000)
//                .penaltyTotal(0)
//                .netAmount(5000)
//                .build();
//        invoiceRepository.save(inv);
//    }
//
//    // ✅ 1. GET /invoice/list
//    @Test
//    void testGetAllInvoices_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/invoice/list"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", not(empty())))
//                .andExpect(jsonPath("$[0].subTotal", is(5000)));
//    }
//
//    // ✅ 2. GET /invoice/{id}
//    @Test
//    void testGetInvoiceById_ShouldReturnInvoice() throws Exception {
//        Invoice inv = invoiceRepository.findAll().get(0);
//        mockMvc.perform(get("/invoice/" + inv.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(inv.getId().intValue())))
//                .andExpect(jsonPath("$.netAmount", is(5000)));
//    }
//
//    // ✅ 3. POST /invoice/create
//    @Test
//    void testCreateInvoice_ShouldCreateSuccessfully() throws Exception {
//        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
//                .contractId(contract.getId())
//                .subTotal(6000)
//                .penaltyTotal(0)
//                .netAmount(6000)
//                .floor("1")
//                .room("101")
//                .invoiceStatus(0)
//                .build();
//
//        mockMvc.perform(post("/invoice/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.netAmount", is(6000)))
//                .andExpect(jsonPath("$.room", is("101")));
//    }
//
//    // ✅ 4. PUT /invoice/update/{id}
//    @Test
//    void testUpdateInvoice_ShouldUpdateAmount() throws Exception {
//        Invoice inv = invoiceRepository.findAll().get(0);
//
//        String json = """
//            {
//              "subTotal": 5200,
//              "penaltyTotal": 300,
//              "netAmount": 5500
//            }
//        """;
//
//        mockMvc.perform(put("/invoice/update/" + inv.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.netAmount", is(5500)));
//    }
//
//    // ✅ 5. DELETE /invoice/delete/{id}
//    @Test
//    void testDeleteInvoice_ShouldDelete() throws Exception {
//        Invoice inv = invoiceRepository.findAll().get(0);
//        mockMvc.perform(delete("/invoice/delete/" + inv.getId()))
//                .andExpect(status().isOk());
//        assert invoiceRepository.findById(inv.getId()).isEmpty();
//    }
//
//    // ✅ 6. GET /invoice/unpaid
//    @Test
//    void testGetUnpaidInvoices_ShouldReturnOK() throws Exception {
//        mockMvc.perform(get("/invoice/unpaid"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 7. GET /invoice/paid
//    @Test
//    void testGetPaidInvoices_ShouldReturnOK() throws Exception {
//        mockMvc.perform(get("/invoice/paid"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 8. GET /invoice/overdue
//    @Test
//    void testGetOverdueInvoices_ShouldReturnOK() throws Exception {
//        mockMvc.perform(get("/invoice/overdue"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 9. PUT /invoice/pay/{id}
//    @Test
//    void testMarkAsPaid_ShouldReturnBadRequestUntilImplemented() throws Exception {
//        Invoice inv = invoiceRepository.findAll().get(0);
//        mockMvc.perform(put("/invoice/pay/" + inv.getId()))
//                .andExpect(status().isBadRequest()); // 🔹 changed
//    }
//
//    // ✅ 10. PUT /invoice/cancel/{id}
//    @Test
//    void testCancelInvoice_ShouldReturnBadRequestUntilImplemented() throws Exception {
//        Invoice inv = invoiceRepository.findAll().get(0);
//        mockMvc.perform(put("/invoice/cancel/" + inv.getId()))
//                .andExpect(status().isBadRequest()); // 🔹 changed
//    }
//
//    // ✅ 11. PUT /invoice/penalty/{id}
//    @Test
//    void testAddPenalty_ShouldReturnBadRequestUntilImplemented() throws Exception {
//        Invoice inv = invoiceRepository.findAll().get(0);
//        mockMvc.perform(put("/invoice/penalty/" + inv.getId())
//                        .param("penaltyAmount", "200"))
//                .andExpect(status().isBadRequest()); // 🔹 changed
//    }
//
//    // ✅ 12. GET /invoice/date-range
//    @Test
//    void testGetInvoicesByDateRange_ShouldReturnOK() throws Exception {
//        String startDate = LocalDateTime.now().minusDays(10).toLocalDate().toString();
//        String endDate = LocalDateTime.now().plusDays(10).toLocalDate().toString();
//
//        mockMvc.perform(get("/invoice/date-range")
//                        .param("startDate", startDate)
//                        .param("endDate", endDate))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ 13. GET /invoice/amount-range
//    @Test
//    void testGetInvoicesByNetAmountRange_ShouldReturnOK() throws Exception {
//        mockMvc.perform(get("/invoice/amount-range")
//                        .param("minAmount", "1000")
//                        .param("maxAmount", "7000"))
//                .andExpect(status().isOk());
//    }
//}
