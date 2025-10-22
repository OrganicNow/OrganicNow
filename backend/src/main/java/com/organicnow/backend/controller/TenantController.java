package com.organicnow.backend.controller;

import com.organicnow.backend.dto.CreateTenantContractRequest;
import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.dto.TenantDetailDto;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import com.organicnow.backend.model.TenantDocument;
import com.organicnow.backend.service.TenantService;
import com.organicnow.backend.service.TenantContractService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tenant")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TenantController {

    private final TenantService tenantService;
    private final TenantContractService tenantContractService;

    public TenantController(TenantService tenantService,
                            TenantContractService tenantContractService) {
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
    // ✅ ค้นหาผู้เช่าด้วย keyword
    @GetMapping("/search")
    public ResponseEntity<List<TenantDocument>> searchTenant(@RequestParam String keyword) {
        List<TenantDocument> results = tenantSearchService.searchByName(keyword);
        return ResponseEntity.ok(results);
    }

    // ✅ Reindex tenant ทั้งหมด (กรณี import จาก PostgreSQL)
    @GetMapping("/reindex")
    public ResponseEntity<String> reindexAll() {
        List<Tenant> tenants = tenantService.getAllTenants();

        List<TenantDocument> docs = tenants.stream()
                .map(t -> TenantDocument.builder()
                        .id(t.getId())
                        .firstName(t.getFirstName())
                        .lastName(t.getLastName())
                        .email(t.getEmail())
                        .phoneNumber(t.getPhoneNumber())
                        .nationalId(t.getNationalId())
                        .build())
                .toList();

        tenantSearchService.indexAll(docs);
        return ResponseEntity.ok("✅ Reindexed " + docs.size() + " tenants.");
    }

}
