//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.model.ContractType;
//import com.organicnow.backend.model.PackagePlan;
//import com.organicnow.backend.repository.ContractRepository;
//import com.organicnow.backend.repository.ContractTypeRepository;
//import com.organicnow.backend.repository.InvoiceRepository;
//import com.organicnow.backend.repository.PackagePlanRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * ✅ Integration Test สำหรับ PackagePlanController (เฉพาะ endpoint ที่มีอยู่จริง)
// * ครอบคลุม: GET /packages, POST /packages, PATCH /packages/{id}/toggle
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//class PackagePlanControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private PackagePlanRepository packagePlanRepository;
//
//    @Autowired
//    private ContractTypeRepository contractTypeRepository;
//
//    @Autowired
//    private ContractRepository contractRepository;
//
//    @Autowired
//    private InvoiceRepository invoiceRepository;
//
//    @BeforeEach
//    void setup() {
//        invoiceRepository.deleteAll();
//        contractRepository.deleteAll();
//        packagePlanRepository.deleteAll();
//        contractTypeRepository.deleteAll();
//    }
//
//    // ✅ 1. CREATE package → status 201
//    @Test
//    @DisplayName("POST /packages → should create new package successfully")
//    void testCreatePackage_ShouldReturnCreated() throws Exception {
//        ContractType contractType = new ContractType();
//        contractType.setName("รายเดือน");
//        contractType.setDuration(1);
//        contractType = contractTypeRepository.saveAndFlush(contractType);
//
//        String jsonBody = """
//            {
//              "price": 1200,
//              "is_active": 1,
//              "contract_type_id": %d
//            }
//            """.formatted(contractType.getId());
//
//        mockMvc.perform(post("/packages")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonBody))
//                .andExpect(status().isCreated());
//
//        assertThat(packagePlanRepository.count()).isEqualTo(1);
//    }
//
//    // ✅ 2. GET /packages → ดึงรายการทั้งหมด
//    @Test
//    @DisplayName("GET /packages → should return list of packages")
//    void testGetAllPackages_ShouldReturnOk() throws Exception {
//        ContractType type = contractTypeRepository.saveAndFlush(
//                new ContractType(null, "6 เดือน", 6)
//        );
//
//        packagePlanRepository.saveAndFlush(
//                PackagePlan.builder()
//                        .contractType(type)
//                        .price(BigDecimal.valueOf(10000))
//                        .isActive(1)
//                        .build()
//        );
//
//        mockMvc.perform(get("/packages"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].price").value(10000));
//    }
//
//    // ✅ 3. POST /packages → deactivate old
//    @Test
//    @DisplayName("POST /packages → should deactivate old plan for same ContractType")
//    void testCreatePackage_ShouldDeactivateOldPlan() throws Exception {
//        ContractType type = contractTypeRepository.saveAndFlush(
//                new ContractType(null, "12 เดือน", 12)
//        );
//
//        PackagePlan oldPlan = packagePlanRepository.saveAndFlush(
//                PackagePlan.builder()
//                        .contractType(type)
//                        .price(BigDecimal.valueOf(8000))
//                        .isActive(1)
//                        .build()
//        );
//
//        String jsonBody = """
//            {
//              "price": 9000,
//              "is_active": 1,
//              "contract_type_id": %d
//            }
//            """.formatted(type.getId());
//
//        mockMvc.perform(post("/packages")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonBody))
//                .andExpect(status().isCreated());
//
//        PackagePlan refreshedOld = packagePlanRepository.findById(oldPlan.getId()).orElseThrow();
//        assertThat(refreshedOld.getIsActive()).isEqualTo(0);
//    }
//
//    // ✅ 4. PATCH /packages/{id}/toggle → toggle สถานะสำเร็จ
//    @Test
//    @DisplayName("PATCH /packages/{id}/toggle → should toggle active status")
//    void testTogglePackageStatus_ShouldToggleSuccessfully() throws Exception {
//        ContractType type = contractTypeRepository.saveAndFlush(
//                new ContractType(null, "ทดลอง", 1)
//        );
//
//        PackagePlan plan = packagePlanRepository.saveAndFlush(
//                PackagePlan.builder()
//                        .contractType(type)
//                        .price(BigDecimal.valueOf(1000))
//                        .isActive(1)
//                        .build()
//        );
//
//        mockMvc.perform(patch("/packages/{id}/toggle", plan.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.is_active").value(0)); // ✅ แก้ตรงนี้
//
//        PackagePlan updated = packagePlanRepository.findById(plan.getId()).orElseThrow();
//        assertThat(updated.getIsActive()).isEqualTo(0);
//    }
//
//}
