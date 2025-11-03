// src/main/java/com/organicnow/backend/repository/MaintenanceScheduleRepository.java
package com.organicnow.backend.repository;

import com.organicnow.backend.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    // ✅ ใช้ Native SQL เพื่อให้ Postgres คำนวณ (DATE(next_due_date) - notify_before_date)
    @Query(value = """
        SELECT ms.*
        FROM maintenance_schedule ms
        WHERE ms.next_due_date IS NOT NULL
          AND ms.notify_before_date IS NOT NULL
          AND (DATE(ms.next_due_date) - ms.notify_before_date) <= CURRENT_DATE
          AND NOT EXISTS (
            SELECT 1
            FROM maintenance_notification_skip sk
            WHERE sk.schedule_id = ms.schedule_id
              AND sk.due_date = DATE(ms.next_due_date)
          )
        ORDER BY ms.next_due_date ASC
        """, nativeQuery = true)
    List<MaintenanceSchedule> findAllDueNotifications();

    // ✅ อันนี้ยังเป็น Derived Query ของ Spring Data ตามเดิม
    List<MaintenanceSchedule> findByNextDueDateBetween(LocalDateTime start, LocalDateTime end);
}
