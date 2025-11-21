package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.MaintenanceNotificationSkip;
import com.organicnow.backend.model.MaintenanceSchedule;
import com.organicnow.backend.repository.MaintenanceNotificationSkipRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Test: MaintenanceNotificationSkipRepository")
class MaintenanceNotificationSkipRepositoryIntegrationTest {

    @Autowired
    private MaintenanceNotificationSkipRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private MaintenanceSchedule schedule1, schedule2, schedule3;
    private MaintenanceNotificationSkip skip1, skip2, skip3;

    @BeforeEach
    void setUp() {
        // Clear all data with CASCADE to handle foreign keys
        entityManager.getEntityManager()
                .createNativeQuery("TRUNCATE TABLE maintenance_notification_skip, maintenance_schedule, " +
                        "asset_event, room_asset, asset, asset_group, payment_proofs, payment_records, " +
                        "invoice_item, invoice, maintain, contract_file, contract, room, " +
                        "package_plan, contract_type, tenant, fee, admin " +
                        "RESTART IDENTITY CASCADE")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Create test data
        setupTestData();
    }

    private void setupTestData() {
        // Create asset groups
        AssetGroup assetGroup1 = new AssetGroup();
        assetGroup1.setAssetGroupName("Electronics");
        entityManager.persistAndFlush(assetGroup1);

        AssetGroup assetGroup2 = new AssetGroup();
        assetGroup2.setAssetGroupName("Furniture");
        entityManager.persistAndFlush(assetGroup2);

        AssetGroup assetGroup3 = new AssetGroup();
        assetGroup3.setAssetGroupName("Appliances");
        entityManager.persistAndFlush(assetGroup3);

        // Create maintenance schedules
        schedule1 = new MaintenanceSchedule();
        schedule1.setScheduleTitle("Air Conditioner Service");
        schedule1.setNextDueDate(LocalDateTime.now().plusMonths(1));
        schedule1.setNotifyBeforeDate(7);
        schedule1.setCycleMonth(3);
        schedule1.setScheduleScope(1);
        schedule1.setAssetGroup(assetGroup1);
        entityManager.persistAndFlush(schedule1);

        schedule2 = new MaintenanceSchedule();
        schedule2.setScheduleTitle("Refrigerator Maintenance");
        schedule2.setNextDueDate(LocalDateTime.now().plusMonths(2));
        schedule2.setNotifyBeforeDate(5);
        schedule2.setCycleMonth(6);
        schedule2.setScheduleScope(1);
        schedule2.setAssetGroup(assetGroup2);
        entityManager.persistAndFlush(schedule2);

        schedule3 = new MaintenanceSchedule();
        schedule3.setScheduleTitle("Washing Machine Checkup");
        schedule3.setNextDueDate(LocalDateTime.now().plusMonths(3));
        schedule3.setNotifyBeforeDate(10);
        schedule3.setCycleMonth(12);
        schedule3.setScheduleScope(0);
        schedule3.setAssetGroup(assetGroup3);
        entityManager.persistAndFlush(schedule3);

        // Create notification skip records
        LocalDate today = LocalDate.now();

        skip1 = new MaintenanceNotificationSkip();
        skip1.setScheduleId(schedule1.getId());
        skip1.setDueDate(today.plusDays(10));
        skip1.setSkippedByUserAt(java.time.Instant.now());
        entityManager.persistAndFlush(skip1);

        skip2 = new MaintenanceNotificationSkip();
        skip2.setScheduleId(schedule2.getId());
        skip2.setDueDate(today.plusDays(20));
        skip2.setSkippedByUserAt(java.time.Instant.now());
        entityManager.persistAndFlush(skip2);

        skip3 = new MaintenanceNotificationSkip();
        skip3.setScheduleId(schedule1.getId());
        skip3.setDueDate(today.plusDays(30));
        skip3.setSkippedByUserAt(java.time.Instant.now());
        entityManager.persistAndFlush(skip3);
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should return true when skip record exists")
    void existsByScheduleIdAndDueDate_ShouldReturnTrueWhenExists() {
        // Given
        Long scheduleId = schedule1.getId();
        LocalDate dueDate = LocalDate.now().plusDays(10);

        // When
        boolean exists = repository.existsByScheduleIdAndDueDate(scheduleId, dueDate);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should return false when skip record not found")
    void existsByScheduleIdAndDueDate_ShouldReturnFalseWhenNotFound() {
        // Given
        Long scheduleId = schedule1.getId();
        LocalDate dueDate = LocalDate.now().plusDays(99); // Date that doesn't exist

        // When
        boolean exists = repository.existsByScheduleIdAndDueDate(scheduleId, dueDate);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should return false for non-existent schedule")
    void existsByScheduleIdAndDueDate_ShouldReturnFalseForNonExistentSchedule() {
        // Given
        Long nonExistentScheduleId = 999L;
        LocalDate dueDate = LocalDate.now().plusDays(10);

        // When
        boolean exists = repository.existsByScheduleIdAndDueDate(nonExistentScheduleId, dueDate);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should distinguish between different schedules")
    void existsByScheduleIdAndDueDate_ShouldDistinguishBetweenDifferentSchedules() {
        // Given
        Long schedule1Id = schedule1.getId();
        Long schedule2Id = schedule2.getId();
        LocalDate commonDate = LocalDate.now().plusDays(10);

        // When
        boolean schedule1Exists = repository.existsByScheduleIdAndDueDate(schedule1Id, commonDate);
        boolean schedule2Exists = repository.existsByScheduleIdAndDueDate(schedule2Id, commonDate);

        // Then
        assertThat(schedule1Exists).isTrue(); // schedule1 has skip on this date
        assertThat(schedule2Exists).isFalse(); // schedule2 doesn't have skip on this date
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should distinguish between different due dates")
    void existsByScheduleIdAndDueDate_ShouldDistinguishBetweenDifferentDueDates() {
        // Given
        Long scheduleId = schedule1.getId();
        LocalDate date1 = LocalDate.now().plusDays(10); // Has skip
        LocalDate date2 = LocalDate.now().plusDays(30); // Has skip
        LocalDate date3 = LocalDate.now().plusDays(50); // Doesn't have skip

        // When
        boolean exists1 = repository.existsByScheduleIdAndDueDate(scheduleId, date1);
        boolean exists2 = repository.existsByScheduleIdAndDueDate(scheduleId, date2);
        boolean exists3 = repository.existsByScheduleIdAndDueDate(scheduleId, date3);

        // Then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(exists3).isFalse();
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should handle past dates")
    void existsByScheduleIdAndDueDate_ShouldHandlePastDates() {
        // Given
        Long scheduleId = schedule2.getId();
        LocalDate pastDate = LocalDate.now().minusDays(10);

        // When
        boolean exists = repository.existsByScheduleIdAndDueDate(scheduleId, pastDate);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should handle today's date")
    void existsByScheduleIdAndDueDate_ShouldHandleTodayDate() {
        // Given
        MaintenanceNotificationSkip skipToday = new MaintenanceNotificationSkip();
        skipToday.setScheduleId(schedule3.getId());
        skipToday.setDueDate(LocalDate.now());
        skipToday.setSkippedByUserAt(java.time.Instant.now());
        entityManager.persistAndFlush(skipToday);

        Long scheduleId = schedule3.getId();
        LocalDate today = LocalDate.now();

        // When
        boolean exists = repository.existsByScheduleIdAndDueDate(scheduleId, today);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should handle far future dates")
    void existsByScheduleIdAndDueDate_ShouldHandleFarFutureDates() {
        // Given
        Long scheduleId = schedule1.getId();
        LocalDate farFutureDate = LocalDate.now().plusYears(5);

        // When
        boolean exists = repository.existsByScheduleIdAndDueDate(scheduleId, farFutureDate);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("save: should successfully create a new skip record")
    void save_ShouldSuccessfullyCreateNewSkipRecord() {
        // Given
        MaintenanceNotificationSkip newSkip = new MaintenanceNotificationSkip();
        newSkip.setScheduleId(schedule2.getId());
        newSkip.setDueDate(LocalDate.now().plusDays(50));
        newSkip.setSkippedByUserAt(java.time.Instant.now());

        // When
        MaintenanceNotificationSkip saved = repository.save(newSkip);
        entityManager.flush();

        // Then
        assertThat(saved.getSkipId()).isNotNull();
        assertThat(repository.existsByScheduleIdAndDueDate(schedule2.getId(), LocalDate.now().plusDays(50))).isTrue();
    }

    @Test
    @DisplayName("delete: should successfully delete a skip record")
    void delete_ShouldSuccessfullyDeleteSkipRecord() {
        // Given
        Long skipId = skip1.getSkipId();
        Long scheduleId = schedule1.getId();
        LocalDate dueDate = LocalDate.now().plusDays(10);
        assertThat(repository.existsByScheduleIdAndDueDate(scheduleId, dueDate)).isTrue();

        // When
        repository.delete(skip1);
        entityManager.flush();

        // Then
        assertThat(repository.findById(skipId)).isEmpty();
        assertThat(repository.existsByScheduleIdAndDueDate(scheduleId, dueDate)).isFalse();
    }

    @Test
    @DisplayName("findAll: should return all skip records")
    void findAll_ShouldReturnAllSkipRecords() {
        // When
        List<MaintenanceNotificationSkip> result = repository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(MaintenanceNotificationSkip::getScheduleId)
                .containsExactlyInAnyOrder(schedule1.getId(), schedule2.getId(), schedule1.getId());
    }

    @Test
    @DisplayName("findById: should return skip record when found")
    void findById_ShouldReturnSkipRecordWhenFound() {
        // Given
        Long skipId = skip1.getSkipId();

        // When
        var result = repository.findById(skipId);

        // Then
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(skip -> {
                    assertThat(skip.getScheduleId()).isEqualTo(schedule1.getId());
                    assertThat(skip.getDueDate()).isEqualTo(LocalDate.now().plusDays(10));
                });
    }

    @Test
    @DisplayName("existsByScheduleIdAndDueDate: should handle same schedule multiple skip dates")
    void existsByScheduleIdAndDueDate_ShouldHandleMultipleSkipDatesForSameSchedule() {
        // Given
        Long scheduleId = schedule1.getId();
        LocalDate date1 = LocalDate.now().plusDays(10); // Has skip
        LocalDate date2 = LocalDate.now().plusDays(30); // Has skip
        LocalDate date3 = LocalDate.now().plusDays(40); // Doesn't have skip

        // When & Then
        assertThat(repository.existsByScheduleIdAndDueDate(scheduleId, date1)).isTrue();
        assertThat(repository.existsByScheduleIdAndDueDate(scheduleId, date2)).isTrue();
        assertThat(repository.existsByScheduleIdAndDueDate(scheduleId, date3)).isFalse();
    }

    @Test
    @DisplayName("count: should return correct number of skip records")
    void count_ShouldReturnCorrectNumberOfSkipRecords() {
        // When
        long count = repository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

}