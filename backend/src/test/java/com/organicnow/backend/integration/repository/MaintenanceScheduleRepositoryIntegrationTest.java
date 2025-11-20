package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.MaintenanceSchedule;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.MaintenanceNotificationSkip;
import com.organicnow.backend.repository.MaintenanceScheduleRepository;

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
@DisplayName("Integration Test: MaintenanceScheduleRepository")
class MaintenanceScheduleRepositoryIntegrationTest {

    @Autowired
    private MaintenanceScheduleRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clear all data before each test to ensure complete isolation
        // Order matters due to foreign key constraints
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM maintenance_notification_skip")
                .executeUpdate();
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM maintenance_schedule")
                .executeUpdate();
        // Delete asset_event first (references asset and room)
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM asset_event")
                .executeUpdate();
        // Delete room_asset (references asset and room)
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM room_asset")
                .executeUpdate();
        // Delete asset (references asset_group)
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM asset")
                .executeUpdate();
        // Now safe to delete asset_group
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM asset_group")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findAllDueNotifications: should return schedules due for notification")
    void findAllDueNotifications_ShouldReturnSchedulesDueForNotification() {
        // Given: Schedule with next due date within range for notification
        LocalDate today = LocalDate.now();
        LocalDateTime nextDueDate = today.plusDays(5).atStartOfDay();

        // Fix: Correctly initializing AssetGroup and setting fields
        AssetGroup assetGroup = new AssetGroup();
        assetGroup.setAssetGroupName("Test Asset Group");
        entityManager.persistAndFlush(assetGroup); // Persist AssetGroup to make sure it's saved and has a valid ID

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setScheduleTitle("Test Equipment A");
        schedule.setNextDueDate(nextDueDate);
        schedule.setNotifyBeforeDate(5); // Notify 5 days in advance
        schedule.setCycleMonth(1);  // Example cycle month value
        schedule.setScheduleScope(1);  // Assuming you need this for scope
        schedule.setAssetGroup(assetGroup);
        schedule.setLastDoneDate(LocalDateTime.now()); // Optional field

        entityManager.persistAndFlush(schedule); // Persist and flush the MaintenanceSchedule

        // When: Fetch all due notifications
        List<MaintenanceSchedule> result = repository.findAllDueNotifications();

        // Then: Assert that the schedule is returned
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduleTitle()).isEqualTo("Test Equipment A");
    }

    @Test
    @DisplayName("findAllDueNotifications: should exclude schedules in skip table")
    void findAllDueNotifications_ShouldExcludeSkippedNotifications() {
        // Given: Schedule due for notification and marked as skipped
        LocalDate today = LocalDate.now();
        LocalDateTime nextDueDate = today.plusDays(5).atStartOfDay();

        // Correctly initializing AssetGroup and persisting it
        AssetGroup assetGroup = new AssetGroup();
        assetGroup.setAssetGroupName("Test Asset Group");
        entityManager.persistAndFlush(assetGroup);

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setScheduleTitle("Test Equipment D");
        schedule.setNextDueDate(nextDueDate);
        schedule.setNotifyBeforeDate(5);
        schedule.setCycleMonth(1);
        schedule.setScheduleScope(1);
        schedule.setAssetGroup(assetGroup);
        schedule.setLastDoneDate(LocalDateTime.now());

        entityManager.persistAndFlush(schedule); // Persist and flush the MaintenanceSchedule

        // Create and persist skip record for this schedule
        MaintenanceNotificationSkip skip = new MaintenanceNotificationSkip();
        skip.setScheduleId(schedule.getId());
        skip.setDueDate(nextDueDate.toLocalDate());
        skip.setSkippedByUserAt(java.time.Instant.now());
        entityManager.persistAndFlush(skip); // Persist the skip record

        // When: Fetch all due notifications
        List<MaintenanceSchedule> result = repository.findAllDueNotifications();

        // Then: Assert that the skipped schedule is excluded
        assertThat(result).isEmpty(); // No schedules should be returned because it's skipped
    }

}