package com.organicnow.backend.repository;

import com.organicnow.backend.model.MaintenanceNotificationSkip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MaintenanceNotificationSkipRepository extends JpaRepository<MaintenanceNotificationSkip, Long> {
    boolean existsByScheduleIdAndDueDate(Long scheduleId, LocalDate dueDate);
}
