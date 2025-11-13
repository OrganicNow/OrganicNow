//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.model.Room;
//import com.organicnow.backend.repository.MaintainRepository;
//import com.organicnow.backend.repository.RoomAssetRepository;
//import com.organicnow.backend.repository.InvoiceRepository;
//import com.organicnow.backend.repository.ContractRepository;
//import com.organicnow.backend.repository.RoomRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//class DashboardControllerIntegrationTest {
//
//    // 🐘 PostgreSQL จำลองจริงด้วย Testcontainers
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
//    @Autowired private MaintainRepository maintainRepository;
//    @Autowired private RoomAssetRepository roomAssetRepository;
//    @Autowired private InvoiceRepository invoiceRepository;
//    @Autowired private ContractRepository contractRepository;
//    @Autowired private RoomRepository roomRepository;
//
//    @BeforeEach
//    void setup() {
//        // 🧹 ลบข้อมูลเรียงตามลำดับ FK ป้องกัน constraint error
//        maintainRepository.deleteAll();
//        roomAssetRepository.deleteAll();
//        invoiceRepository.deleteAll();
//        contractRepository.deleteAll();
//        roomRepository.deleteAll();
//
//        // 🏠 เพิ่มห้องจำลอง 2 ห้อง (เทสได้แม้ไม่มี maintain/invoice)
//        Room r1 = new Room();
//        r1.setRoomNumber("A101");
//        r1.setRoomFloor(1);
//
//        Room r2 = new Room();
//        r2.setRoomNumber("A102");
//        r2.setRoomFloor(1);
//
//        roomRepository.save(r1);
//        roomRepository.save(r2);
//    }
//
//    @Test
//    void testDashboardController_UsesRealPostgresContainer() throws Exception {
//        mockMvc.perform(get("/dashboard")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andDo(result -> System.out.println("🔍 Response JSON: " + result.getResponse().getContentAsString()))
//                // ✅ ตรวจเฉพาะโครงสร้างหลักของ JSON
//                .andExpect(jsonPath("$.rooms").isArray())
//                .andExpect(jsonPath("$.maintains").isArray())
//                .andExpect(jsonPath("$.finances").isArray());
//    }
//}
