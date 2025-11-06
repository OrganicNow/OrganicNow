package com.organicnow.backend.controller;

import com.organicnow.backend.dto.CreateTenantContractRequest;
import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.dto.TenantDetailDto;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.service.TenantService;
import com.organicnow.backend.service.TenantContractService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tenant")
@CrossOrigin(origins = {"http://localhost:5173", "http://app.localtest.me"}, allowCredentials = "true")
public class TenantController {

    private final TenantService tenantService;
    private final TenantContractService tenantContractService;

    public TenantController(TenantService tenantService, TenantContractService tenantContractService) {
        this.tenantService = tenantService;
        this.tenantContractService = tenantContractService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(tenantService.list());
    }

    @PostMapping("/create")
    public ResponseEntity<TenantDto> create(@RequestBody CreateTenantContractRequest req) {
        TenantDto dto = tenantContractService.create(req);
        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping("/update/{contractId}")
    public ResponseEntity<TenantDto> update(@PathVariable Long contractId,
                                            @RequestBody UpdateTenantContractRequest req) {
        TenantDto dto = tenantContractService.update(contractId, req);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete/{contractId}")
    public ResponseEntity<Void> delete(@PathVariable Long contractId) {
        tenantContractService.delete(contractId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contractId:\\d+}")
    public ResponseEntity<TenantDetailDto> detail(@PathVariable Long contractId) {
        TenantDetailDto dto = tenantContractService.getDetail(contractId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{contractId:\\d+}/pdf")
    public ResponseEntity<byte[]> downloadContractPdf(@PathVariable Long contractId) {
        byte[] pdfBytes = tenantContractService.generateContractPdf(contractId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=tenant_" + contractId + "_contract.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // 🔍 search แบบพิมพ์ผิดได้ (format เหมือน /list)
    @GetMapping("/search")
    public ResponseEntity<?> searchTenant(@RequestParam String keyword) {
        Map<String, Object> resp = tenantService.searchTenantWithFuzzy(keyword);
        return ResponseEntity.ok(resp);
    }
}