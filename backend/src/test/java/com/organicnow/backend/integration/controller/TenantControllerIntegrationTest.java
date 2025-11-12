//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.dto.CreateTenantContractRequest;
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * ✅ Full Integration Test สำหรับ TenantController
// * ครอบคลุมทุกชั้น: Controller → Service → Repository → Database → PDF Generation
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class TenantControllerIntegrationTest {
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper mapper;
//
//    @Autowired private TenantRepository tenantRepository;
//    @Autowired private RoomRepository roomRepository;
//    @Autowired private PackagePlanRepository packagePlanRepository;
//    @Autowired private ContractTypeRepository contractTypeRepository;
//    @Autowired private ContractRepository contractRepository;
//
//    private Room testRoom;
//    private PackagePlan testPackage;
//
//    @BeforeEach
//    void setup() {
//        // ✅ สร้าง Room
//        testRoom = roomRepository.save(
//                Room.builder()
//                        .roomFloor(1)
//                        .roomNumber("A101")
//                        .build()
//        );
//
//        // ✅ สร้าง ContractType และ PackagePlan
//        ContractType type = contractTypeRepository.save(
//                ContractType.builder()
//                        .name("Monthly")
//                        .duration(6)
//                        .build()
//        );
//
//        testPackage = packagePlanRepository.save(
//                PackagePlan.builder()
//                        .contractType(type)
//                        .price(BigDecimal.valueOf(12000))
//                        .isActive(1)
//                        .build()
//        );
//    }
//
//    // ====================================
//    // 🔹 CREATE tenant + contract
//    // ====================================
//    @Test
//    @DisplayName("POST /tenant/create → should insert tenant + contract into DB")
//    void testCreate_ShouldInsertData() throws Exception {
//        CreateTenantContractRequest req = CreateTenantContractRequest.builder()
//                .firstName("Alice")
//                .lastName("Wonder")
//                .email("alice@example.com")
//                .phoneNumber("0891234567")
//                .nationalId("9999999999999") // ✅ ใช้เลขใหม่
//                .roomId(testRoom.getId())
//                .packageId(testPackage.getId())
//                .signDate(LocalDateTime.now())
//                .startDate(LocalDateTime.now().plusDays(1))
//                .endDate(LocalDateTime.now().plusMonths(3))
//                .deposit(BigDecimal.valueOf(5000))
//                .rentAmountSnapshot(BigDecimal.valueOf(12000))
//                .build();
//
//        mockMvc.perform(post("/tenant/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.firstName").value("Alice"));
//    }
//
//    // ====================================
//    // 🔹 LIST tenant
//    // ====================================
//    @Test
//    @DisplayName("GET /tenant/list → should contain newly created tenant in the list")
//    void testList_ShouldReturnTenantList() throws Exception {
//        // ✅ สร้าง tenant และ contract ใหม่ 1 รายการ เพื่อให้แน่ใจว่ามี Bob อยู่จริง
//        Tenant tenant = tenantRepository.save(
//                Tenant.builder()
//                        .firstName("Bob")
//                        .lastName("Builder")
//                        .phoneNumber("0812345678")
//                        .email("bob@example.com")
//                        .nationalId("9998887776665") // ใช้เลขใหม่เพื่อไม่ชนกับ data.sql
//                        .build()
//        );
//
//        Contract contract = contractRepository.save(
//                Contract.builder()
//                        .tenant(tenant)
//                        .room(testRoom)
//                        .packagePlan(testPackage)
//                        .signDate(LocalDateTime.now())
//                        .startDate(LocalDateTime.now())
//                        .endDate(LocalDateTime.now().plusMonths(6))
//                        .deposit(BigDecimal.valueOf(5000))
//                        .rentAmountSnapshot(BigDecimal.valueOf(12000))
//                        .status(1)
//                        .build()
//        );
//
//        // ✅ ตรวจสอบว่ามี Bob อยู่ในผลลัพธ์
//        mockMvc.perform(get("/tenant/list"))
//                .andExpect(status().isOk())
//                // ตรวจว่ามีอย่างน้อย 1 คนชื่อ Bob ใน list
//                .andExpect(jsonPath("$.results[?(@.firstName == 'Bob')]").exists())
//                // ตรวจว่าข้อมูลของ Bob มีห้องที่ถูกต้อง
//                .andExpect(jsonPath("$.results[?(@.firstName == 'Bob')].room").value("A101"));
//    }
//
//
//    // ====================================
//    // 🔹 DELETE tenant contract
//    // ====================================
//    @Test
//    @DisplayName("DELETE /tenant/delete/{contractId} → should delete contract from DB")
//    void testDelete_ShouldRemoveContractFromDatabase() throws Exception {
//        Tenant tenant = tenantRepository.save(
//                Tenant.builder()
//                        .firstName("Charlie")
//                        .lastName("Brown")
//                        .phoneNumber("0823456789")
//                        .email("charlie@example.com")
//                        .nationalId("9876543210987")
//                        .build()
//        );
//
//        Contract contract = contractRepository.save(
//                Contract.builder()
//                        .tenant(tenant)
//                        .room(testRoom)
//                        .packagePlan(testPackage)
//                        .signDate(LocalDateTime.now())
//                        .startDate(LocalDateTime.now())
//                        .endDate(LocalDateTime.now().plusMonths(6))
//                        .deposit(BigDecimal.valueOf(5000))
//                        .rentAmountSnapshot(BigDecimal.valueOf(12000))
//                        .status(1)
//                        .build()
//        );
//
//        // ✅ ใช้ contractId ที่ถูกต้อง
//        mockMvc.perform(delete("/tenant/delete/" + contract.getId()))
//                .andExpect(status().isNoContent());
//
//        assertThat(contractRepository.existsById(contract.getId())).isFalse();
//    }
//
//    // ====================================
//    // 🔹 DETAIL tenant contract
//    // ====================================
//    @Test
//    @DisplayName("GET /tenant/{contractId} → should return detail from DB")
//    void testDetail_ShouldReturnTenantDetail() throws Exception {
//        Tenant tenant = tenantRepository.save(
//                Tenant.builder()
//                        .firstName("Dana")
//                        .lastName("Lee")
//                        .phoneNumber("0834567890")
//                        .email("dana@example.com")
//                        .nationalId("5555555555555")
//                        .build()
//        );
//
//        Contract contract = contractRepository.save(
//                Contract.builder()
//                        .tenant(tenant)
//                        .room(testRoom)
//                        .packagePlan(testPackage)
//                        .signDate(LocalDateTime.now())
//                        .startDate(LocalDateTime.now())
//                        .endDate(LocalDateTime.now().plusMonths(6))
//                        .deposit(BigDecimal.valueOf(5000))
//                        .rentAmountSnapshot(BigDecimal.valueOf(12000))
//                        .status(1)
//                        .build()
//        );
//
//        mockMvc.perform(get("/tenant/" + contract.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.firstName").value("Dana"))
//                .andExpect(jsonPath("$.room").value("A101"))
//                .andExpect(jsonPath("$.packageName").value("Monthly"));
//    }
//
//    // ====================================
//    // 🔹 GENERATE CONTRACT PDF
//    // ====================================
//    @Test
//    @DisplayName("GET /tenant/{contractId}/pdf → should return valid PDF bytes")
//    void testDownloadPdf_ShouldReturnPdf() throws Exception {
//        Tenant tenant = tenantRepository.save(
//                Tenant.builder()
//                        .firstName("Erin")
//                        .lastName("Stone")
//                        .phoneNumber("0845678901")
//                        .email("erin@example.com")
//                        .nationalId("1111222233334")
//                        .build()
//        );
//
//        Contract contract = contractRepository.save(
//                Contract.builder()
//                        .tenant(tenant)
//                        .room(testRoom)
//                        .packagePlan(testPackage)
//                        .signDate(LocalDateTime.now())
//                        .startDate(LocalDateTime.now())
//                        .endDate(LocalDateTime.now().plusMonths(6))
//                        .deposit(BigDecimal.valueOf(5000))
//                        .rentAmountSnapshot(BigDecimal.valueOf(12000))
//                        .status(1)
//                        .build()
//        );
//
//        mockMvc.perform(get("/tenant/" + contract.getId() + "/pdf"))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Disposition", "attachment; filename=tenant_" + contract.getId() + "_contract.pdf"))
//                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
//                .andExpect(result -> {
//                    byte[] pdfBytes = result.getResponse().getContentAsByteArray();
//                    assertThat(pdfBytes.length).isGreaterThan(100);
//                });
//    }
//}
