package com.organicnow.backend.controller;

import com.organicnow.backend.dto.NotificationDueDto;
import com.organicnow.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/notifications", "/api/notifications"})
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173",
        "http://localhost:3000",
        "http://app.localtest.me",
        "https://transcondylar-noncorporately-christen.ngrok-free.dev"}, allowCredentials = "true")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/due")
    public List<NotificationDueDto> getDue(/* Principal principal */) {
        Long userId = null; // ถ้ามี multi-user ให้ดึงจาก principal
        return notificationService.getDueNotifications(userId);
    }

    // ปุ่ม "กากบาท" = skip noti รอบนี้ (ระบุตาม scheduleId + dueDate)
    @DeleteMapping("/schedule/{scheduleId}/due/{dueDate}/skip")
    public ResponseEntity<Void> skip(
            @PathVariable Long scheduleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
    ) {
        notificationService.skipScheduleDue(scheduleId, dueDate);
        return ResponseEntity.noContent().build();
    }
}
