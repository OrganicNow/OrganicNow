package com.organicnow.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_notification_skip",
        uniqueConstraints = @UniqueConstraint(name = "ux_mns_schedule_due", columnNames = {"schedule_id","due_date"}))
public class MaintenanceNotificationSkip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skip_id")
    private Long skipId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "skipped_by_user_at", nullable = false)
    private Instant skippedByUserAt = Instant.now();

    public Long getSkipId() { return skipId; }
    public void setSkipId(Long skipId) { this.skipId = skipId; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Instant getSkippedByUserAt() { return skippedByUserAt; }
    public void setSkippedByUserAt(Instant skippedByUserAt) { this.skippedByUserAt = skippedByUserAt; }
}
