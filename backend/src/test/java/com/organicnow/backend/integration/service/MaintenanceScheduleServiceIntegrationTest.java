package com.organicnow.backend.integration.service;

import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.MaintenanceScheduleRepository;
import com.organicnow.backend.service.MaintenanceScheduleService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MaintenanceScheduleServiceIntegrationTest {


    // ---------------- Testcontainers ----------------
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // ปิด scheduler ทั้งหมด
        registry.add("spring.task.scheduling.enabled", () -> "false");
    }

    // ---------------- Inject Service, Repo ----------------
    @Autowired private MaintenanceScheduleService scheduleService;
    @Autowired private MaintenanceScheduleRepository scheduleRepo;
    @Autowired private AssetGroupRepository assetGroupRepo;


    // ---------------- Disable Scheduler (prevent background tasks) ----------------
    @Configuration
    @ComponentScan(basePackages = "com.organicnow.backend")   // ⭐ สำคัญมาก — บอก Spring ให้สแกน Bean ทั้งหมด
    static class DisableSchedulerConfig {

        @Bean
        public TaskScheduler taskScheduler() {
            TaskScheduler scheduler = mock(TaskScheduler.class);

            // mock ทุกเมทอดที่ scheduler อาจเรียก
            when(scheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(null);
            when(scheduler.schedule(any(Runnable.class), any(java.util.Date.class))).thenReturn(null);
            when(scheduler.schedule(any(Runnable.class), any(org.springframework.scheduling.Trigger.class))).thenReturn(null);
            when(scheduler.scheduleAtFixedRate(any(Runnable.class), any(java.util.Date.class), anyLong())).thenReturn(null);
            when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong())).thenReturn(null);
            when(scheduler.scheduleWithFixedDelay(any(Runnable.class), any(java.util.Date.class), anyLong())).thenReturn(null);
            when(scheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong())).thenReturn(null);

            return scheduler;
        }
    }


    // ---------------- Test Setup ----------------
    AssetGroup group;

    @BeforeEach
    void setup() {
        group = new AssetGroup();
        group.setAssetGroupName("Electrical");
        group = assetGroupRepo.save(group);
    }

    // ---------------- Tests ----------------

    @Test
    void testCreateSchedule() {

        MaintenanceScheduleCreateDto dto = MaintenanceScheduleCreateDto.builder()
                .scheduleScope(0)
                .scheduleTitle("Aircon Cleaning")
                .scheduleDescription("Clean filter")
                .cycleMonth(3)
                .assetGroupId(group.getId())
                .lastDoneDate(LocalDateTime.now())
                .nextDueDate(LocalDateTime.now().plusMonths(3))
                .notifyBeforeDate(7)
                .build();

        MaintenanceScheduleDto created = scheduleService.createSchedule(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getScheduleTitle()).isEqualTo("Aircon Cleaning");
        assertThat(created.getAssetGroupId()).isEqualTo(group.getId());
    }

    @Test
    void testUpdateSchedule() {
        var saved = scheduleService.createSchedule(
                MaintenanceScheduleCreateDto.builder()
                        .scheduleScope(0)
                        .scheduleTitle("Check water pump")
                        .cycleMonth(6)
                        .assetGroupId(group.getId())
                        .build()
        );

        MaintenanceScheduleCreateDto updateDto = MaintenanceScheduleCreateDto.builder()
                .scheduleScope(0)
                .scheduleTitle("Check water pump UPDATED")
                .cycleMonth(12)
                .assetGroupId(group.getId())
                .build();

        MaintenanceScheduleDto updated = scheduleService.updateSchedule(saved.getId(), updateDto);

        assertThat(updated.getScheduleTitle()).isEqualTo("Check water pump UPDATED");
        assertThat(updated.getCycleMonth()).isEqualTo(12);
    }

    @Test
    void testDeleteSchedule() {
        var dto = scheduleService.createSchedule(
                MaintenanceScheduleCreateDto.builder()
                        .scheduleScope(0)
                        .scheduleTitle("Fire alarm inspection")
                        .cycleMonth(1)
                        .assetGroupId(group.getId())
                        .build()
        );

        scheduleService.deleteSchedule(dto.getId());

        assertThat(scheduleRepo.findById(dto.getId())).isEmpty();
    }

    @Test
    void testMarkAsDone() {
        var dto = scheduleService.createSchedule(
                MaintenanceScheduleCreateDto.builder()
                        .scheduleScope(0)
                        .scheduleTitle("Fan cleaning")
                        .cycleMonth(2)
                        .assetGroupId(group.getId())
                        .build()
        );

        MaintenanceScheduleDto done = scheduleService.markAsDone(dto.getId());

        assertThat(done.getLastDoneDate()).isNotNull();
        assertThat(done.getNextDueDate()).isNotNull();
    }
}
