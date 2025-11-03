// src/main/java/com/organicnow/backend/service/MaintenanceScheduleService.java
package com.organicnow.backend.service;

import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.MaintenanceSchedule;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.MaintenanceScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaintenanceScheduleService {

    private final MaintenanceScheduleRepository scheduleRepo;
    private final AssetGroupRepository assetGroupRepo;

    /** ✅ สร้าง schedule ใหม่ */
    public MaintenanceScheduleDto createSchedule(MaintenanceScheduleCreateDto dto) {
        MaintenanceSchedule s = new MaintenanceSchedule();
        applyDtoToEntity(dto, s);
        MaintenanceSchedule saved = scheduleRepo.save(s);

        // 🔄 สถาปัตยกรรมใหม่: ไม่ต้องสร้าง/เช็ค notification ตรงนี้
        // Notification จะถูกคำนวณจาก maintenance_schedule ตอนเรียก /api/notifications/due

        return toDto(saved);
    }

    /** ✅ แก้ไข schedule */
    public MaintenanceScheduleDto updateSchedule(Long id, MaintenanceScheduleCreateDto dto) {
        MaintenanceSchedule s = scheduleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + id));
        applyDtoToEntity(dto, s);
        return toDto(scheduleRepo.save(s));
    }

    /** ✅ ดึงทั้งหมด */
    @Transactional(readOnly = true)
    public List<MaintenanceScheduleDto> getAllSchedules() {
        return scheduleRepo.findAll().stream().map(this::toDto).toList();
    }

    /** ✅ ดึงตาม id */
    @Transactional(readOnly = true)
    public Optional<MaintenanceScheduleDto> getScheduleById(Long id) {
        return scheduleRepo.findById(id).map(this::toDto);
    }

    /** ✅ ลบ */
    public void deleteSchedule(Long id) {
        // ตรวจสอบว่า schedule มีอยู่จริงหรือไม่
        MaintenanceSchedule schedule = scheduleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + id));

        // 🔄 ไม่ต้องลบ notification เดิม เพราะเราไม่ได้เก็บ notification entity แล้ว
        // (มีแค่ตาราง skip ซึ่งผูก FK ON DELETE CASCADE ที่ schedule อยู่แล้ว หากสร้างตามที่แนะนำ)

        scheduleRepo.deleteById(id);
        log.info("Deleted maintenance schedule: {}", id);
    }

    /** ✅ มาร์กงานเสร็จ */
    public MaintenanceScheduleDto markAsDone(Long id) {
        MaintenanceSchedule s = scheduleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + id));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        s.setLastDoneDate(now);

        Integer months = s.getCycleMonth() != null ? s.getCycleMonth() : 0;
        s.setNextDueDate(months > 0 ? now.plusMonths(months) : null);

        return toDto(scheduleRepo.save(s));
    }

    /** ✅ งานที่จะครบกำหนดภายใน X วัน */
    @Transactional(readOnly = true)
    public List<MaintenanceScheduleDto> getUpcomingSchedules(int days) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return scheduleRepo.findByNextDueDateBetween(start, end)
                .stream().map(this::toDto).toList();
    }

    // ---------------- helpers ----------------

    /** map DTO → Entity ให้ครบ */
    private void applyDtoToEntity(MaintenanceScheduleCreateDto dto, MaintenanceSchedule s) {
        s.setScheduleScope(dto.getScheduleScope());
        s.setCycleMonth(dto.getCycleMonth());
        s.setNotifyBeforeDate(dto.getNotifyBeforeDate());
        s.setScheduleTitle(dto.getScheduleTitle());
        s.setScheduleDescription(dto.getScheduleDescription());
        s.setLastDoneDate(dto.getLastDoneDate());
        s.setNextDueDate(dto.getNextDueDate());

        if (dto.getAssetGroupId() != null) {
            AssetGroup ag = assetGroupRepo.findById(dto.getAssetGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("AssetGroup not found: " + dto.getAssetGroupId()));
            s.setAssetGroup(ag);
        } else {
            s.setAssetGroup(null);
        }
    }

    /** แปลง Entity → DTO */
    private MaintenanceScheduleDto toDto(MaintenanceSchedule s) {
        return MaintenanceScheduleDto.builder()
                .id(s.getId())
                .scheduleScope(s.getScheduleScope())
                .assetGroupId(s.getAssetGroup() != null ? s.getAssetGroup().getId() : null)
                .assetGroupName(s.getAssetGroup() != null ? s.getAssetGroup().getAssetGroupName() : null)
                .cycleMonth(s.getCycleMonth())
                .lastDoneDate(s.getLastDoneDate())
                .nextDueDate(s.getNextDueDate())
                .notifyBeforeDate(s.getNotifyBeforeDate())
                .scheduleTitle(s.getScheduleTitle())
                .scheduleDescription(s.getScheduleDescription())
                .build();
    }
}
