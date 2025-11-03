package com.organicnow.backend.service.impl;

import com.organicnow.backend.dto.NotificationDueDto;
import com.organicnow.backend.model.MaintenanceNotificationSkip;
import com.organicnow.backend.model.MaintenanceSchedule;
import com.organicnow.backend.repository.MaintenanceNotificationSkipRepository;
import com.organicnow.backend.repository.MaintenanceScheduleRepository;
import com.organicnow.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MaintenanceScheduleRepository scheduleRepo;
    private final MaintenanceNotificationSkipRepository skipRepo;

    private static final ZoneId ZONE_TH = ZoneId.of("Asia/Bangkok");

    @Override
    public List<NotificationDueDto> getDueNotifications(Long userId) {
        final LocalDate today = LocalDate.now(ZONE_TH);

        // ดึงทุก schedule (ถ้ามี flag is_active ก็เปลี่ยนมาใช้ findAllByIsActiveTrue())
        final List<MaintenanceSchedule> schedules = scheduleRepo.findAll();

        List<NotificationDueDto> result = new ArrayList<>();

        for (MaintenanceSchedule s : schedules) {
            // เงื่อนไขแจ้งเตือน = วันนี้ >= (dueDate - notify_before)
            int before = (s.getNotifyBeforeDate() == null) ? 0 : Math.max(0, s.getNotifyBeforeDate());

            // ✅ พิจารณาเป็นราย due-date: ทั้ง lastDoneDate และ nextDueDate
            List<LocalDateTime> candidates = new ArrayList<>();
            if (s.getLastDoneDate() != null) {
                candidates.add(s.getLastDoneDate());   // “Last date” บน UI
            }
            if (s.getNextDueDate() != null) {
                candidates.add(s.getNextDueDate());    // “Next date” บน UI
            }

            for (LocalDateTime candidate : candidates) {
                LocalDate dueDate = candidate.toLocalDate();

                // ข้ามถ้า user เคยกด skip คู่ (scheduleId, dueDate) นี้แล้ว
                if (skipRepo.existsByScheduleIdAndDueDate(s.getId(), dueDate)) {
                    continue;
                }

                LocalDate notifyOn = dueDate.minusDays(before);

                // เข้า window แล้ว => สร้าง noti หนึ่งรายการสำหรับ dueDate นี้
                if (!today.isBefore(notifyOn)) {
                    NotificationDueDto dto = new NotificationDueDto();
                    dto.setScheduleId(s.getId());
                    dto.setNextDueDate(dueDate); // DTO ของคุณใช้ชื่อ nextDueDate เป็นฟิลด์ 'วันที่ถึงกำหนด' (เราส่ง dueDate ตัวที่กำลังพิจารณา)
                    dto.setNotifyAt(notifyOn.atStartOfDay(ZONE_TH).toInstant());

                    String title = (s.getScheduleTitle() != null && !s.getScheduleTitle().isBlank())
                            ? s.getScheduleTitle()
                            : "Maintenance due soon";
                    String desc = (s.getScheduleDescription() == null) ? "" : s.getScheduleDescription();

                    String msg = String.format(
                            "%s is due on %s%s",
                            title,
                            dueDate,
                            (before > 0 ? String.format(" (%dd notice)", before) : "")
                    );

                    dto.setTitle(title);
                    dto.setMessage(msg + (desc.isBlank() ? "" : " " + desc));
                    dto.setActionUrl("/maintenance?scheduleId=" + s.getId());

                    result.add(dto);
                }
            }
        }

        // เรียงเก่าสุด -> ใหม่สุดตามเวลาที่ควรแจ้ง
        result.sort(Comparator.comparing(NotificationDueDto::getNotifyAt));
        return result;
    }

    @Override
    public void skipScheduleDue(Long scheduleId, LocalDate dueDate) {
        if (!skipRepo.existsByScheduleIdAndDueDate(scheduleId, dueDate)) {
            MaintenanceNotificationSkip sk = new MaintenanceNotificationSkip();
            sk.setScheduleId(scheduleId);
            sk.setDueDate(dueDate);
            skipRepo.save(sk);
        }
    }

    @Override
    public void checkAndCreateDueNotifications() {
        // no-op: ไม่ได้ใช้ scheduler เก็บ state แล้ว
    }

    @Override
    public void skipScheduleDueDate(Long scheduleId, LocalDate dueDate) {
        // optional: duplicate ของ skipScheduleDue ถ้า interface มีสองชื่อ
        skipScheduleDue(scheduleId, dueDate);
    }
}
