//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.repository.AssetGroupRepository;
//import com.organicnow.backend.repository.MaintenanceScheduleRepository;
//import com.organicnow.backend.repository.NotificationRepository;
//import com.organicnow.backend.service.MaintenanceScheduleService;
//import com.organicnow.backend.service.NotificationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
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
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.doNothing;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * ✅ Integration Test for MaintenanceScheduleController
// * Using real PostgreSQL database through Testcontainers (PostgreSQL 17)
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class MaintenanceScheduleControllerIntegrationTest {
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
//    @Autowired private AssetGroupRepository assetGroupRepository;
//    @Autowired private MaintenanceScheduleRepository scheduleRepository;
//    @Autowired private NotificationRepository notificationRepository;
//    @Autowired private MaintenanceScheduleService scheduleService;
//
//    @MockBean private NotificationService notificationService;
//
//    private AssetGroup group;
//    private Long createdScheduleId;
//
//    private static final DateTimeFormatter ISO_FMT =
//            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//
//    @BeforeEach
//    void setup() {
//        // ✅ Clean all tables before each test
//        jdbcTemplate.execute("""
//            TRUNCATE TABLE notifications, maintenance_schedule, asset_group, asset, room_asset, room
//            RESTART IDENTITY CASCADE
//        """);
//
//        // ✅ Mock NotificationService (to avoid actual side effects)
//        doNothing().when(notificationService)
//                .createMaintenanceScheduleNotification(org.mockito.ArgumentMatchers.any());
//        doNothing().when(notificationService).checkAndCreateDueNotifications();
//        doNothing().when(notificationService)
//                .deleteNotificationsByMaintenanceSchedule(org.mockito.ArgumentMatchers.anyLong());
//
//        // ✅ Create AssetGroup
//        group = assetGroupRepository.save(
//                AssetGroup.builder().assetGroupName("Electrical System").build()
//        );
//
//        // ✅ Create a schedule using service to ensure DB data consistency
//        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
//        dto.setScheduleScope(0);
//        dto.setAssetGroupId(group.getId());
//        dto.setCycleMonth(6);
//        dto.setNotifyBeforeDate(3);
//        dto.setScheduleTitle("Check Air Conditioner");
//        dto.setScheduleDescription("Routine AC inspection");
//        dto.setLastDoneDate(LocalDateTime.now().minusMonths(6));
//        dto.setNextDueDate(LocalDateTime.now().plusDays(3));
//
//        var created = scheduleService.createSchedule(dto);
//        createdScheduleId = created.getId();
//    }
//
//    // ✅ 1. GET /schedules/{id}
//    @Test
//    @DisplayName("GET /schedules/{id} → should return schedule by id")
//    void testGetScheduleById_ShouldReturnItem() throws Exception {
//        mockMvc.perform(get("/schedules/{id}", createdScheduleId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(createdScheduleId.intValue())))
//                .andExpect(jsonPath("$.scheduleTitle", is("Check Air Conditioner")))
//                .andExpect(jsonPath("$.assetGroupId", is(group.getId().intValue())));
//    }
//
//    // ✅ 2. POST /schedules
//    @Test
//    @DisplayName("POST /schedules → should create new schedule successfully")
//    void testCreateSchedule_ShouldReturnOK() throws Exception {
//        String body = """
//        {
//          "scheduleScope": 0,
//          "assetGroupId": %d,
//          "cycleMonth": 3,
//          "notifyBeforeDate": 5,
//          "scheduleTitle": "Water Pump Check",
//          "scheduleDescription": "Routine water pump check",
//          "lastDoneDate": "%s",
//          "nextDueDate": "%s"
//        }
//        """.formatted(
//                group.getId(),
//                LocalDateTime.now().minusMonths(3).format(ISO_FMT),
//                LocalDateTime.now().plusMonths(3).format(ISO_FMT)
//        );
//
//        mockMvc.perform(post("/schedules")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", notNullValue()))
//                .andExpect(jsonPath("$.scheduleTitle", is("Water Pump Check")));
//    }
//
//    // ✅ 3. PUT /schedules/{id}
//    @Test
//    @DisplayName("PUT /schedules/{id} → should update schedule successfully")
//    void testUpdateSchedule_ShouldReturnOK() throws Exception {
//        String body = """
//        {
//          "scheduleScope": 0,
//          "assetGroupId": %d,
//          "cycleMonth": 12,
//          "notifyBeforeDate": 10,
//          "scheduleTitle": "AC Deep Inspection",
//          "scheduleDescription": "Annual AC maintenance",
//          "lastDoneDate": "%s",
//          "nextDueDate": "%s"
//        }
//        """.formatted(
//                group.getId(),
//                LocalDateTime.now().minusMonths(1).format(ISO_FMT),
//                LocalDateTime.now().plusMonths(12).format(ISO_FMT)
//        );
//
//        mockMvc.perform(put("/schedules/{id}", createdScheduleId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.scheduleTitle", is("AC Deep Inspection")))
//                .andExpect(jsonPath("$.cycleMonth", is(12)));
//    }
//
//    // ✅ 4. GET /schedules
//    @Test
//    @DisplayName("GET /schedules → should return list of schedules and assetGroupDropdown")
//    void testGetAllSchedules_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/schedules"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result", not(empty())))
//                .andExpect(jsonPath("$.assetGroupDropdown", not(empty())));
//    }
//
//    // ✅ 5. GET /schedules/upcoming?days=7
//    @Test
//    @DisplayName("GET /schedules/upcoming?days=7 → should return upcoming schedules")
//    void testGetUpcomingSchedules_ShouldReturnList() throws Exception {
//        mockMvc.perform(get("/schedules/upcoming").param("days", "7"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result", not(empty())))
//                .andExpect(jsonPath("$.assetGroupDropdown", not(empty())));
//    }
//
//    // ✅ 6. PATCH /schedules/{id}/done
//    @Test
//    @DisplayName("PATCH /schedules/{id}/done → should mark schedule as done")
//    void testMarkAsDone_ShouldReturnOK() throws Exception {
//        mockMvc.perform(patch("/schedules/{id}/done", createdScheduleId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(createdScheduleId.intValue())))
//                .andExpect(jsonPath("$.lastDoneDate", notNullValue()));
//    }
//
//    // ✅ 7. DELETE /schedules/{id}
//    @Test
//    @DisplayName("DELETE /schedules/{id} → should delete schedule successfully")
//    void testDeleteSchedule_ShouldReturnNoContent() throws Exception {
//        mockMvc.perform(delete("/schedules/{id}", createdScheduleId))
//                .andExpect(status().isNoContent());
//    }
//}
