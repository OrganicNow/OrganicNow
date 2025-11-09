package com.organicnow.backend.service;

import com.organicnow.backend.dto.NotificationDueDto;

import java.time.LocalDate;
import java.util.List;

public interface NotificationService {
    List<NotificationDueDto> getDueNotifications(Long userId);  // ถ้า single-tenant จะไม่ใช้ userId ก็ได้
    void skipScheduleDue(Long scheduleId, LocalDate dueDate);
    void checkAndCreateDueNotifications();
    /** บันทึกว่า scheduleId งวด dueDate นี้ถูก “ข้าม” ไปแล้ว */
    void skipScheduleDueDate(Long scheduleId, LocalDate dueDate);
}
