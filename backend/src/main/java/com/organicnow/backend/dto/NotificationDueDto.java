package com.organicnow.backend.dto;

import java.time.Instant;
import java.time.LocalDate;

public class NotificationDueDto {
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleDescription;
    private LocalDate nextDueDate;
    private Integer notifyDays;    // = notify_before_date
    private Instant notifyAt;      // = (next_due_date - notifyDays) startOfDay(Asia/Bangkok)
    private String actionUrl;      // เช่น /maintenance?scheduleId=...

    // ข้อความที่ประกอบจาก schedule (ไม่เก็บใน DB)
    private String title;          // "Maintenance due soon"
    private String message;        // "กำหนดตรวจ 2025-11-15 (แจ้งล่วงหน้า 7 วัน)"

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public String getScheduleTitle() { return scheduleTitle; }
    public void setScheduleTitle(String scheduleTitle) { this.scheduleTitle = scheduleTitle; }
    public String getScheduleDescription() { return scheduleDescription; }
    public void setScheduleDescription(String scheduleDescription) { this.scheduleDescription = scheduleDescription; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public Integer getNotifyDays() { return notifyDays; }
    public void setNotifyDays(Integer notifyDays) { this.notifyDays = notifyDays; }
    public Instant getNotifyAt() { return notifyAt; }
    public void setNotifyAt(Instant notifyAt) { this.notifyAt = notifyAt; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
