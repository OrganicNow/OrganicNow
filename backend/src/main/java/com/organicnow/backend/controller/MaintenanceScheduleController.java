package com.organicnow.backend.controller;

import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import com.organicnow.backend.dto.MaintenanceScheduleResponse;
import com.organicnow.backend.service.AssetGroupService;
import com.organicnow.backend.service.MaintenanceScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@CrossOrigin(origins = {"http://localhost:5173", "http://app.localtest.me"}, allowCredentials = "true")
@RequiredArgsConstructor
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;
    private final AssetGroupService   assetGroupService;

    /** ✅ สร้าง schedule ใหม่ */
    @PostMapping
    public ResponseEntity<MaintenanceScheduleDto> create(@RequestBody MaintenanceScheduleCreateDto dto) {
        return ResponseEntity.ok(scheduleService.createSchedule(dto));
    }

    /** ✅ อัปเดต schedule */
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceScheduleDto> update(
            @PathVariable Long id,
            @RequestBody MaintenanceScheduleCreateDto dto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, dto));
    }

    /** ✅ ดึงทั้งหมด + assetGroupDropdown */
    @GetMapping
    public ResponseEntity<MaintenanceScheduleResponse> getAll() {
        List<MaintenanceScheduleDto> schedules = scheduleService.getAllSchedules();
        var groups = assetGroupService.getAllGroupsForDropdown();

        MaintenanceScheduleResponse response = MaintenanceScheduleResponse.builder()
                .result(schedules)
                .assetGroupDropdown(groups)
                .build();

        return ResponseEntity.ok(response);
    }

    /** ✅ ดึงตาม id */
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceScheduleDto> getById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** ✅ ลบ */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    /** ✅ มาร์กว่างานเสร็จ */
    @PatchMapping("/{id}/done")
    public ResponseEntity<MaintenanceScheduleDto> markAsDone(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.markAsDone(id));
    }

    /** ✅ ดึงงานที่จะครบกำหนดใน X วัน (default 7) + assetGroupDropdown */
    @GetMapping("/upcoming")
    public ResponseEntity<MaintenanceScheduleResponse> getUpcoming(
            @RequestParam(defaultValue = "7") int days) {
        List<MaintenanceScheduleDto> schedules = scheduleService.getUpcomingSchedules(days);
        var groups = assetGroupService.getAllGroupsForDropdown();

        MaintenanceScheduleResponse response = MaintenanceScheduleResponse.builder()
                .result(schedules)
                .assetGroupDropdown(groups)
                .build();

        return ResponseEntity.ok(response);
    }
}
