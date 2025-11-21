package com.organicnow.backend.controller;

import com.organicnow.backend.dto.PackagePlanDto;
import com.organicnow.backend.dto.PackagePlanRequestDto;
import com.organicnow.backend.service.PackagePlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/packages", "/api/packages"})
@CrossOrigin(origins = {"http://localhost:5173",
        "http://app.localtest.me",
        "https://transcondylar-noncorporately-christen.ngrok-free.dev"}, allowCredentials = "true")
public class PackagePlanController {

    private final PackagePlanService packagePlanService;

    public PackagePlanController(PackagePlanService packagePlanService) {
        this.packagePlanService = packagePlanService;
    }

    // GET /packages - list ทั้งหมด
    @GetMapping
    public ResponseEntity<List<PackagePlanDto>> listPackages() {
        List<PackagePlanDto> items = packagePlanService.getAllPackages();
        return ResponseEntity.ok(items);
    }

    // POST /packages - สร้างแพ็คเกจใหม่ (จะ deactivate ตัวเก่าที่ชื่อ contract type ซ้ำ)
    @PostMapping
    public ResponseEntity<Void> createPackage(@Valid @RequestBody PackagePlanRequestDto dto) {
        packagePlanService.createPackage(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // PATCH /packages/{id}/toggle - สลับสถานะ isActive 0<->1
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PackagePlanDto> toggleStatus(@PathVariable Long id) {
        PackagePlanDto updated = packagePlanService.togglePackageStatus(id);
        return ResponseEntity.ok(updated);
    }

    /*
     * ถ้าภายหลังอยาก "ตั้งค่า" สถานะแบบกำหนดตรง ๆ (เช่น 0 หรือ 1)
     * ให้เพิ่มเมธอดใน Service: updatePackageStatus(Long id, Integer isActive)
     * แล้วค่อยปลดคอมเมนต์ endpoint ด้านล่างนี้
     *
     * @PatchMapping("/{id}/status")
     * public ResponseEntity<PackagePlanDto> setStatus(
     *         @PathVariable Long id,
     *         @RequestBody StatusRequest body
     * ) {
     *     PackagePlanDto updated = packagePlanService.updatePackageStatus(id, body.getIsActive());
     *     return ResponseEntity.ok(updated);
     * }
     *
     * public static class StatusRequest {
     *     private Integer isActive; // 0 หรือ 1
     *     public Integer getIsActive() { return isActive; }
     *     public void setIsActive(Integer isActive) { this.isActive = isActive; }
     * }
     */
}


//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.PackagePlanDto;
//import com.organicnow.backend.dto.PackagePlanRequestDto;
//import com.organicnow.backend.service.PackagePlanService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/packages")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
//public class PackagePlanController {
//
//    private final PackagePlanService packagePlanService;
//
//    public PackagePlanController(PackagePlanService packagePlanService) {
//        this.packagePlanService = packagePlanService;
//    }
//
//    @GetMapping
//    public ResponseEntity<List<PackagePlanDto>> getAllPackages() {
//        return ResponseEntity.ok(packagePlanService.getAllPackages());
//    }
//
//    @PostMapping
//    public ResponseEntity<String> createPackage(@RequestBody PackagePlanRequestDto dto) {
//        packagePlanService.createPackage(dto);
//        return ResponseEntity.status(HttpStatus.CREATED).body("Package saved successfully");
//    }
//}
