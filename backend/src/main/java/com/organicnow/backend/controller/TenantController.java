package com.organicnow.backend.controller;

import com.organicnow.backend.dto.CreateTenantContractRequest;
import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.dto.TenantDetailDto;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import com.organicnow.backend.service.TenantService;
import com.organicnow.backend.service.TenantContractService;
import com.organicnow.backend.service.ContractFileService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping({"/tenant", "/api/tenant"})
@CrossOrigin(origins = {"http://localhost:5173",
        "http://app.localtest.me",
        "https://transcondylar-noncorporately-christen.ngrok-free.dev"}, allowCredentials = "true")
public class TenantController {

    private final TenantService tenantService;
    private final TenantContractService tenantContractService;
    private final ContractFileService contractFileService; // ✅ service ใหม่

    public TenantController(
            TenantService tenantService,
            TenantContractService tenantContractService,
            ContractFileService contractFileService
    ) {
        this.tenantService = tenantService;
        this.tenantContractService = tenantContractService;
        this.contractFileService = contractFileService;
    }

    // ✅ list tenants (ของเดิม + เพิ่ม flag ใหม่แบบไม่กระทบ)
    @GetMapping("/list")
    public ResponseEntity<?> list() {
        Object result = tenantService.list();

        // ถ้าเป็น Map เช่น { "results": [...], "total": 10 }
        if (result instanceof Map<?, ?> map) {
            Object rows = map.get("results");
            if (rows instanceof List<?>) {
                for (Object obj : (List<?>) rows) {
                    if (obj instanceof TenantDto dto) {
                        boolean hasFile = contractFileService.hasSignedFile(dto.getContractId());
                        dto.setHasSignedPdf(hasFile);
                    }
                }
            }
            return ResponseEntity.ok(map);
        }

        // ถ้าเป็น List<TenantDto> ตรง ๆ (รองรับของเก่าทุกแบบ)
        if (result instanceof List<?>) {
            for (Object obj : (List<?>) result) {
                if (obj instanceof TenantDto dto) {
                    boolean hasFile = contractFileService.hasSignedFile(dto.getContractId());
                    dto.setHasSignedPdf(hasFile);
                }
            }
            return ResponseEntity.ok(result);
        }

        // fallback
        return ResponseEntity.ok(result);
    }

    // ✅ create tenant contract
    @PostMapping("/create")
    public ResponseEntity<TenantDto> create(@RequestBody CreateTenantContractRequest req) {
        TenantDto dto = tenantContractService.create(req);
        return ResponseEntity.status(201).body(dto);
    }

    // ✅ update tenant contract
    @PutMapping("/update/{contractId}")
    public ResponseEntity<TenantDto> update(
            @PathVariable Long contractId,
            @RequestBody UpdateTenantContractRequest req
    ) {
        TenantDto dto = tenantContractService.update(contractId, req);
        return ResponseEntity.ok(dto);
    }

    // ✅ delete tenant contract
    @DeleteMapping("/delete/{contractId}")
    public ResponseEntity<Void> delete(@PathVariable Long contractId) {
        tenantContractService.delete(contractId);
        // ✅ ลบไฟล์สัญญา (ถ้ามี)
        contractFileService.deleteByContractId(contractId);
        return ResponseEntity.noContent().build();
    }

    // ✅ detail view
    @GetMapping("/{contractId:\\d+}")
    public ResponseEntity<TenantDetailDto> detail(@PathVariable Long contractId) {
        TenantDetailDto dto = tenantContractService.getDetail(contractId);
        return ResponseEntity.ok(dto);
    }

    // ✅ download unsigned contract (ของเดิม)
    @GetMapping("/{contractId:\\d+}/pdf")
    public ResponseEntity<byte[]> downloadContractPdf(@PathVariable Long contractId) {
        byte[] pdfBytes = tenantContractService.generateContractPdf(contractId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=tenant_" + contractId + "_contract.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ✅ 🆕 download signed contract
    @GetMapping("/{contractId:\\d+}/pdf/signed")
    public ResponseEntity<?> downloadSignedContract(@PathVariable Long contractId) {
        byte[] file = contractFileService.getSignedFile(contractId);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No signed contract found for this tenant");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=tenant_" + contractId + "_signed.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    // ✅ 🆕 upload signed contract
    @PostMapping("/{contractId:\\d+}/pdf/upload")
    public ResponseEntity<?> uploadSignedContract(
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            contractFileService.uploadSignedFile(contractId, file);
            return ResponseEntity.ok("Signed contract uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    // 🔍 search tenants (ของเดิม + เพิ่ม flag)
    @GetMapping("/search")
    public ResponseEntity<?> searchTenant(@RequestParam String keyword) {
        Map<String, Object> resp = tenantService.searchTenantWithFuzzy(keyword);

        Object rows = resp.get("results");
        if (rows instanceof List<?>) {
            for (Object obj : (List<?>) rows) {
                if (obj instanceof TenantDto dto) {
                    boolean hasFile = contractFileService.hasSignedFile(dto.getContractId());
                    dto.setHasSignedPdf(hasFile);
                }
            }
        }

        return ResponseEntity.ok(resp);
    }
}