package com.organicnow.backend.controller;

import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.dto.ApiResponse;
import com.organicnow.backend.dto.CreateMaintainRequest;
import com.organicnow.backend.dto.MaintainDto;
import com.organicnow.backend.dto.UpdateMaintainRequest;
import com.organicnow.backend.service.MaintainRoomService;  // ใช้ MaintainRoomService
import com.organicnow.backend.service.MaintainService;  // ใช้ MaintainService
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintain")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4173", "http://app.localtest.me"}, allowCredentials = "true")
@RequiredArgsConstructor
public class MaintainController {

    private final MaintainRoomService maintainRoomService;  // ใช้ MaintainRoomService สำหรับ Room-related logic
    private final MaintainService maintainService;  // ใช้ MaintainService สำหรับ MaintainDto-related logic

    @GetMapping("/{roomId}/requests")
    public ApiResponse<List<RequestDto>> getRequestsByRoom(@PathVariable Long roomId) {
        List<RequestDto> requests = maintainRoomService.getRequestsByRoomId(roomId);  // เรียกใช้ MaintainRoomService
        return new ApiResponse<>("success", requests);
    }

    @GetMapping("/list")
    public ResponseEntity<List<MaintainDto>> list() {
        return ResponseEntity.ok(maintainService.getAll());  // ใช้ MaintainService
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintainDto> get(@PathVariable Long id) {
        return maintainService.getById(id)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateMaintainRequest req) {
        try {
            return ResponseEntity.ok(maintainService.create(req));  // ใช้ MaintainService
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Create failed: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateMaintainRequest req) {
        try {
            return ResponseEntity.ok(maintainService.update(id, req));  // ใช้ MaintainService
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            maintainService.delete(id);  // ใช้ MaintainService
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Delete failed: " + e.getMessage());
        }
    }

    // ✅ PDF Generation Endpoint
    @GetMapping("/{id}/report-pdf")
    public ResponseEntity<byte[]> generateMaintenanceReportPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = maintainService.generateMaintenanceReportPdf(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "maintenance-report-" + id + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            System.err.println("PDF generation failed: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
